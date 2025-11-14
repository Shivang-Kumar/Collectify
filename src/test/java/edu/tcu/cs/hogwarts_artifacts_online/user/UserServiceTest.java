package edu.tcu.cs.hogwarts_artifacts_online.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import edu.tcu.cs.hogwarts_artifacts_online.Wizard.Wizard;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.Artifact;
import edu.tcu.cs.hogwarts_artifacts_online.rediscache.RedisCacheClient;
import edu.tcu.cs.hogwarts_artifacts_online.system.ObjectNotFoundException;
import edu.tcu.cs.hogwarts_artifacts_online.system.exception.PasswordChangeIllegalArgumentException;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value="dev")
public class UserServiceTest {

	@Mock
	UserRepository userRepository;
	
	@Mock
	PasswordEncoder passwordEncoder;
	
	@Mock
	RedisCacheClient redisCacheClient;

	@InjectMocks
	UserService userService;

	@Test
	void testFindAllUserSuccess() {

		User u1 = new User();
		u1.setId(1);
		u1.setUsername("ABC_1");
		u1.setRoles("User");
		u1.setEnabled(true);
		u1.setPassword("jkc");

		User u2 = new User();
		u2.setId(2);
		u2.setUsername("ABC_2");
		u2.setRoles("Admin");
		u2.setEnabled(true);
		u2.setPassword("jhk");

		List<User> findUsers = new ArrayList<>();
		findUsers.add(u1);
		findUsers.add(u2);
		// given
		given(userRepository.findAll()).willReturn(findUsers);

		// when

		List<User> actualAllUsers = userService.findAllUser();

		// then
		assertThat(actualAllUsers.size()).isEqualTo(findUsers.size());
		verify(userRepository, times(1)).findAll();

	}

	@Test
	void testaddUserSuccess() {
		// given

		User user = new User();

		user.setId(1);
		user.setEnabled(true);
		user.setPassword("Asdfghjkl");
		user.setUsername("My Name");
		user.setRoles("user");

		given(userRepository.save(user)).willReturn(user);
		given(this.passwordEncoder.encode(user.getPassword())).willReturn("Encoded Password");
		// when
		User savedUser = userService.addUser(user);

		// then
		assertThat(savedUser.getId()).isEqualTo(1);
		assertThat(savedUser.getUsername()).isEqualTo("My Name");
		assertThat(savedUser.getRoles()).isEqualTo(user.getRoles());
		assertThat(savedUser.isEnabled()).isEqualTo(user.isEnabled());
		verify(userRepository, times(1)).save(user);

	}

	@Test
	void testFindUserByIdSuccess() {
		// Given. Arrange Inputs and targets. Define the behaviour of mock object
		// artifactRepository.
		User u1 = new User();
		u1.setId(1);
		u1.setUsername("ABC_1");
		u1.setRoles("User");
		u1.setEnabled(true);
		u1.setPassword("jkc");

		given(userRepository.findById(1)).willReturn(Optional.of(u1));

		// When. Act on the target behavior . When steps should cover the method to be
		// tested.

		User returnedUser = userService.findUserById(1);

		// Then. Assert expected outcomes.

		assertThat(returnedUser.getId()).isEqualTo(u1.getId());
		assertThat(returnedUser.getUsername()).isEqualTo(u1.getUsername());
		assertThat(returnedUser.isEnabled()).isEqualTo(u1.isEnabled());
		assertThat(returnedUser.getPassword()).isEqualTo(u1.getPassword());
		verify(userRepository, times(1)).findById(1);
	}

	@Test
	void testFindUserByIdNotFound() {
		// Given.

		given(this.userRepository.findById(Mockito.any(Integer.class))).willReturn(Optional.empty());

		// When.

		Throwable thrown = catchThrowable(() -> {
			User returnedUser = userService.findUserById(1);
		});

		// Then.
		assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find user with id 1  :(");
		verify(this.userRepository, times(1)).findById(1);

	}

	@Test
	void testUserUpdateSuccess() {

		User u3 = new User();
		u3.setId(1);
		u3.setUsername("ABC_Update");
		u3.setRoles("User admin");
		u3.setEnabled(false);
		u3.setPassword("jkc");

		given(userRepository.findById(1)).willReturn(Optional.of(u3));
		given(userRepository.save(u3)).willReturn(u3);

		// When

		User savedUser = userService.updateUser(1, u3);

		// Then

		assertThat(savedUser.getId()).isEqualTo(1);
		assertThat(savedUser.getUsername()).isEqualTo(u3.getUsername());
		verify(userRepository, times(1)).findById(1);
		verify(userRepository, times(1)).save(u3);

	}

	@Test
	void testUserUpdateNotFound() {
		// Given
		User u3 = new User();
		u3.setId(1);
		u3.setUsername("ABC_Update");
		u3.setRoles("User admin");
		u3.setEnabled(false);
		u3.setPassword("jkc");

		given(userRepository.findById(1)).willReturn(Optional.empty());

		// When
		assertThrows(ObjectNotFoundException.class, () -> {
			userService.updateUser(1, u3);
		});

		// Then

		verify(userRepository, times(1)).findById(1);
	}

	@Test
	void testDeleteSuccess() {
		// given
		User u3 = new User();
		u3.setId(1);
		u3.setUsername("ABC_Update");
		u3.setRoles("User admin");
		u3.setEnabled(false);
		u3.setPassword("jkc");


		given(userRepository.findById(1)).willReturn(Optional.of(u3));
		doNothing().when(userRepository).deleteById(1);

		// when
		userService.deleteUserById(1);
		// then
		verify(userRepository, times(1)).deleteById(1);

	}
	
	@Test
	void testDeleteNotFound()
	{
		//given 
		
		given(userRepository.findById(1)).willReturn(Optional.empty());
		
		//Above method will throw optional so below method is not required as it will not be executed
		//doNothing().when(artifactRepository).deleteById("13445324535632");
		
		
		//when
		assertThrows(ObjectNotFoundException.class, () -> {
			userService.deleteUserById(1);
		});
		//then
		verify(userRepository,times(1)).findById(1);
		
	}
	
	
	@Test
	void testChangePasswordSuccess()
	{
		//Given 
		
		User hogwartsUser=new User();
		hogwartsUser.setId(2);
		hogwartsUser.setPassword("encryptedOldPassword");
		given(this.userRepository.findById(2)).willReturn(Optional.of(hogwartsUser));
		given(this.passwordEncoder.matches(anyString(),anyString())).willReturn(true);
		given(this.passwordEncoder.encode(anyString())).willReturn("encryptedNewPassword");
		given(this.userRepository.save(hogwartsUser)).willReturn(hogwartsUser);
		doNothing().when(this.redisCacheClient).delete(anyString());
		//When 
		this.userService.changePassword(2, "unencryptedOldPassword", "@1Abcdefg", "@1Abcdefg");
		
		
		//then
		
		assertThat(hogwartsUser.getPassword()).isEqualTo("encryptedNewPassword");
		verify(this.userRepository,times(1)).save(hogwartsUser);
		
	}
	
	
	@Test
	void testChangePasswordOldPasswordIsIncorrect()
	{
				//Given 
		
				User hogwartsUser=new User();
				hogwartsUser.setId(2);
				hogwartsUser.setPassword("encryptedOldPassword");
				given(this.userRepository.findById(2)).willReturn(Optional.of(hogwartsUser));
				given(this.passwordEncoder.matches(anyString(),anyString())).willReturn(false);

				//When 
				Exception exception=assertThrows(PasswordChangeIllegalArgumentException.class,()->{
					this.userService.changePassword(2,"wrongOldPassword","ABC123", "ABC123");
				});
				
				
				//then
				
				assertThat(exception).isInstanceOf(PasswordChangeIllegalArgumentException.class).hasMessage("Old Password is incorrect.");
				
		
	}
	
	
	@Test
	void testChangePasswordNewPasswordDoesNotMatchConfirmNewPassword()
	{
		//Given 
		
		User hogwartsUser=new User();
		hogwartsUser.setId(2);
		hogwartsUser.setPassword("encryptedOldPassword");
		given(this.userRepository.findById(2)).willReturn(Optional.of(hogwartsUser));
		given(this.passwordEncoder.matches(anyString(),anyString())).willReturn(true);
		

		//When 
		Exception exception=assertThrows(PasswordChangeIllegalArgumentException.class,()->{
			this.userService.changePassword(2,"unencryptedOldPassword","ABC123", "ABC1235hsadj");
		});
		
		
		//then
		
		assertThat(exception).isInstanceOf(PasswordChangeIllegalArgumentException.class).hasMessage("New Password and Confirm new Password does not match");
		
	}
	
	@Test
	void testChangePasswordNewPasswordDoesNotConfirmToPolicy(){
		//Given 
		
		User hogwartsUser=new User();
		hogwartsUser.setId(2);
		hogwartsUser.setPassword("encryptedOldPassword");
		given(this.userRepository.findById(2)).willReturn(Optional.of(hogwartsUser));
		given(this.passwordEncoder.matches(anyString(),anyString())).willReturn(true);

		//When 
		Exception exception=assertThrows(PasswordChangeIllegalArgumentException.class,()->{
			this.userService.changePassword(2,"unencryptedOldPassword","short", "short");
		});
		
		
		//then
		
		assertThat(exception).isInstanceOf(PasswordChangeIllegalArgumentException.class).hasMessage("New Password does not follow password policy");
		
	}
	
	
	@Test
	void testChangePasswordUserNotFound()
	{
		
		//Given 
		given(this.userRepository.findById(2)).willReturn(Optional.empty());

		//When 
		Exception exception=assertThrows(ObjectNotFoundException.class,()->{
			this.userService.changePassword(2,"wrongOldPassword","ABC123", "ABC123");
		});
		
		
		//then
		
		assertThat(exception).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find user with id 2  :(");
		
		
	}
	
	
	

}
