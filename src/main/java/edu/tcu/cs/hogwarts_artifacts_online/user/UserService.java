package edu.tcu.cs.hogwarts_artifacts_online.user;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.tcu.cs.hogwarts_artifacts_online.rediscache.RedisCacheClient;
import edu.tcu.cs.hogwarts_artifacts_online.system.ObjectNotFoundException;
import edu.tcu.cs.hogwarts_artifacts_online.system.exception.PasswordChangeIllegalArgumentException;

@Service
public class UserService implements UserDetailsService {
	
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private RedisCacheClient redisCacheClient;

	
	
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,RedisCacheClient redisCacheClient) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.redisCacheClient= redisCacheClient;
	}


	public List<User> findAllUser(){
		List<User> allUsers=this.userRepository.findAll();
		return allUsers;
	}


	public User addUser(User user) {
		user.setPassword(this.passwordEncoder.encode(user.getPassword()));
		return this.userRepository.save(user);
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
	


	public void changePassword(Integer userId, String oldPassword, String newPassword, String confirmPassword) {
		
	User hogwartsUser=	this.userRepository.findById(userId).orElseThrow(()-> new ObjectNotFoundException("user", userId));
	
	//If old Password is not correct , throw an exception
	if(!this.passwordEncoder.matches(oldPassword, hogwartsUser.getPassword()))
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
	
	hogwartsUser.setPassword(this.passwordEncoder.encode(newPassword));
	
	
	
	//Revoke users's current jwt token by deleting in redis before saving new password
	this.redisCacheClient.delete("whitelist:"+userId);
	
	this.userRepository.save(hogwartsUser);
	
	}

}
