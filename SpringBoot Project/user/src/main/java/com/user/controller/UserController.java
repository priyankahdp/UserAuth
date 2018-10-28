package com.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;
import com.user.exception.UserNotFoundException;
import com.user.model.UserAuthBean;
import com.user.model.UserProfileBean;
import com.user.model.UserRequestBean;
import com.user.service.IUserService;
import com.user.status.UserTokenStatus;
import com.user.util.IValidateHeaderToken;
import com.user.util.PasswordUtils;

/**
 * The Class UserController.
 */
@RestController
@Component
public class UserController {
	
	/** The user service. */
	@Autowired
	private IUserService userService;
	
	@Autowired
	private IValidateHeaderToken tokenValidator; 

	/**
	 * User Login function.
	 *
	 * @param LoginBean 
	 * @return the ResponseEntity
	 * @throws UserNotFoundException 
	 */
	@PostMapping("/users")
	public ResponseEntity<String> login(@RequestBody UserAuthBean userAuthBean) {
		boolean isValidUser=userService.isValidUser(userAuthBean);
		if (isValidUser) {
			//if valid user issue token
			String userToken=userService.issueToken(userAuthBean.getUsername());
			return new ResponseEntity<String>("{\"token\":\""+userToken+"\"}", HttpStatus.OK);			
		}else {
			//if no username 401
			return new ResponseEntity<String>("{\"error\": \"Invalid username/password\"}", HttpStatus.UNAUTHORIZED);
		}
	}
	
    /**
	 * Gets the user profile for given accessToken
	 *
	 * @param userName
	 * @param authorizationHeaderToken
	 * @return the userProfile or errors if records notExists
	 */
	@GetMapping("/users/{username}")
	public ResponseEntity<String> getUserProfile(@PathVariable("username") String userName,@RequestHeader(value="Authorization") String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			return new ResponseEntity<String>("Please add token header eg :- Authorization Bearer [YOUR_TOKEN]", HttpStatus.UNAUTHORIZED);
        }else {
        	UserTokenStatus status=tokenValidator.validateAuthToken(userName,authorizationHeader);
			if (status==UserTokenStatus.NOUSERFOUND) {
				//no users found		404
				//{"message": "No users found"}
				return new ResponseEntity<String>("{\"message\": \"No users found\"}",HttpStatus.NOT_FOUND);				
			}else if (status==UserTokenStatus.INVALIDTOKEN) {
				//if incorrect token	401
				//{"message": "Invalid token"}
				return new ResponseEntity<String>("{\"message\": \"Invalid token\"}",HttpStatus.UNAUTHORIZED);
			}else if (status==UserTokenStatus.SUCCESS) {
				//User found	200
				//{"Username”: “abc123”,“Fullname”: “John Doe”,“PhoneNumber”: “+1234567890”}
				UserProfileBean profileBean=userService.getUserProfile(userName, authorizationHeader);
				Gson gson=new Gson();
				return new ResponseEntity<String>(gson.toJson(profileBean),HttpStatus.OK);					
			}else {
				return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR); 
			}
			
		}	
	}
	
	/**
	 * Insert Dummy users (testing purpose)
	 *
	 * @param UserBean
	 * @param UriBuilder for return news inserted record's location
	 * @return the newly added user's profile
	 */
	@PostMapping("/users/add")
	public ResponseEntity<String> addDummyData(@RequestBody UserRequestBean userRequestBean,UriComponentsBuilder builder) {
		PasswordUtils utils=new PasswordUtils();
		// generate random SALT
		String saltValue = utils.getSaltValue(20);
		// creating password with SALT
		userRequestBean.setPassword(utils.generatePasswordUsingSalt(userRequestBean.getPassword(), saltValue));
		userRequestBean.setSaltValue(saltValue);
		
		boolean responseStatus = userService.addUsers(userRequestBean);
        if (responseStatus == false) {
        	return new ResponseEntity<String>(HttpStatus.CONFLICT);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/users/{username}").buildAndExpand(userRequestBean.getUsername()).toUri());
        return new ResponseEntity<String>(headers, HttpStatus.OK);        
	}	
}