package edu.tcu.cs.hogwarts_artifacts_online.user;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.tcu.cs.hogwarts_artifacts_online.notification.Notification;
import edu.tcu.cs.hogwarts_artifacts_online.notification.NotificationChannel;
import edu.tcu.cs.hogwarts_artifacts_online.notification.NotificationEventPublisher;
import edu.tcu.cs.hogwarts_artifacts_online.rediscache.RedisCacheClient;
import edu.tcu.cs.hogwarts_artifacts_online.system.ObjectNotFoundException;
import edu.tcu.cs.hogwarts_artifacts_online.system.exception.PasswordChangeIllegalArgumentException;

@Service
public class UserService implements UserDetailsService {
	
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private RedisCacheClient redisCacheClient;
    private NotificationEventPublisher publisher;
	
	
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,RedisCacheClient redisCacheClient,NotificationEventPublisher publisher) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.redisCacheClient= redisCacheClient;
		this.publisher=publisher;
	}


	public List<User> findAllUser(){
		List<User> allUsers=this.userRepository.findAll();
		return allUsers;
	}


	public User addUser(User user) {
		user.setPassword(this.passwordEncoder.encode(user.getPassword()));
		User savedUser=this.userRepository.save(user);
		Notification notify=createNotification(user);
		publisher.publish(notify);
		return savedUser;

	}
	
	public User findUserById(Integer id)
	{
		User foundUser=this.userRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("user", id));
		return foundUser;
	}


	public User updateUser(Integer userId,User user) {
	return this.userRepository.findById(userId).map(foundUser -> {
		foundUser.setUsername(user.getUsername());
		foundUser.setEnabled(user.isEnabled());
		foundUser.setRoles(user.getRoles());
		return this.userRepository.save(foundUser);
	}).orElseThrow(() ->  new ObjectNotFoundException("user", userId));
	}


	public void deleteUserById(Integer userId) {
		
		User user=this.userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("user", userId));
		this.userRepository.deleteById(userId);
	}


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	return this.userRepository.findByUsername(username)
				.map(user -> new MyUserPrincipal(user))
				.orElseThrow(() -> new UsernameNotFoundException("username"+username+" is not found."));
	}
	
	
	
	
	
	private Notification createNotification(User user)
	{
		Notification notify=Notification.builder()
				.eventId(UUID.randomUUID().toString())
				.traceId(UUID.randomUUID().toString())
				.channel(NotificationChannel.EMAIL)
				.recipient(user.getUsername())
				.templateId("USER-CREATED")
				.payload(Map.of("name",user.getUsername()))
				.build();
		return notify;
	}
	


	public void changePassword(Integer userId, String oldPassword, String newPassword, String confirmPassword) {
		
	User user=	this.userRepository.findById(userId).orElseThrow(()-> new ObjectNotFoundException("user", userId));
	

	
	//If old Password is not correct , throw an exception
	if(!this.passwordEncoder.matches(oldPassword, user.getPassword()))
	{
		throw new PasswordChangeIllegalArgumentException("Old Password is incorrect.");
	}
	
	//If new Password and confirm new Password does not match , throw an exception
	if(!newPassword.equals(confirmPassword))
	{
		throw new PasswordChangeIllegalArgumentException("New Password and Confirm new Password does not match");
	}
	
	// The new Password must contain at least one digit, one lowercase, one uppercase letter, and one special character
	String passwordPolicy = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!()_{}\\[\\]:;\"'<>,.?/~`|\\\\-]).+$";

	if(!newPassword.matches(passwordPolicy))
	{
		throw new PasswordChangeIllegalArgumentException("New Password does not follow password policy");
	}
	
	user.setPassword(this.passwordEncoder.encode(newPassword));
	
	
	
	//Revoke users's current jwt token by deleting in redis before saving new password
	this.redisCacheClient.delete("whitelist:"+userId);
	
	this.userRepository.save(user);
	
	}
	
	public void changePasswordByOtp(Integer userId, String newPassword, String confirmPassword, String resetToken) {
		
		
		User user=	this.userRepository.findById(userId).orElseThrow(()-> new ObjectNotFoundException("user", userId));
		
		String resetTokenKey=resetTokenKey(user.getId());
		if(verifyResetToken(resetTokenKey,resetToken)==false)
		throw new RuntimeException("Reset token has expired or is invalid");
		
		
		
		//If new Password and confirm new Password does not match , throw an exception
		if(!newPassword.equals(confirmPassword))
		{
			throw new PasswordChangeIllegalArgumentException("New Password and Confirm new Password does not match");
		}
		
		// The new Password must contain at least one digit, one lowercase, one uppercase letter, and one special character
		String passwordPolicy = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!()_{}\\[\\]:;\"'<>,.?/~`|\\\\-]).+$";

		if(!newPassword.matches(passwordPolicy))
		{
			throw new PasswordChangeIllegalArgumentException("New Password does not follow password policy");
		}
		
		user.setPassword(this.passwordEncoder.encode(newPassword));
		
		
		
		//Revoke users's current jwt token by deleting in redis before saving new password
		this.redisCacheClient.delete("whitelist:"+userId);
		
		this.redisCacheClient.delete(resetTokenKey);
		this.userRepository.save(user);
		
		}


	public String generateOtp(String username) {
		User user=this.userRepository.findByUsername(username).orElseThrow(() -> new ObjectNotFoundException("user",username));
		String randomOtp=randomOtpGenerator();
		String hashOtp=this.passwordEncoder.encode(randomOtp);
		String key=otpKeyGenerator(user.getId());
		this.redisCacheClient.set(key,hashOtp,2, TimeUnit.MINUTES);
		return randomOtp;
	}
	
	private String otpKeyGenerator(Integer userId)
	{
		String key="otp:user:"+userId;
		return key;
	}
	
	private String resetTokenKey(Integer userId)
	{
		String key="reset-token:user:"+userId;
		return key;
	}
	
	private String randomOtpGenerator()
	{
		    SecureRandom random = new SecureRandom();
	        int otp = 100000 + random.nextInt(900000);
	        return otp+"";
	}


	public String verifyOtp(String username, String otp) {
		User user=this.userRepository.findByUsername(username).orElseThrow(() -> new ObjectNotFoundException("user",username));
		String key=otpKeyGenerator(user.getId());
		String storedHashedOtp=this.redisCacheClient.get(key);
		if(storedHashedOtp!=null && this.passwordEncoder.matches(otp,storedHashedOtp))
		{
			
			System.out.println("Code matched herer===================");
			String resetTokenKey=resetTokenKey(user.getId());
			String resetTokenValue=UUID.randomUUID().toString();
			this.redisCacheClient.set(resetTokenKey,resetTokenValue,5,TimeUnit.MINUTES);
			this.redisCacheClient.delete(otpKeyGenerator(user.getId()));
			return resetTokenValue;
		}
		
		return null;
	}
	
	public boolean verifyResetToken(String resetTokenKey,String token)
	{
		String storedResetToken=this.redisCacheClient.get(resetTokenKey);
		return storedResetToken.equals(token)?true:false;
	}

}
