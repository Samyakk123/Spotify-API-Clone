package com.csc301.profilemicroservice;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.csc301.profilemicroservice.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class ProfileController {
	public static final String KEY_USER_NAME = "userName";
	public static final String KEY_USER_FULLNAME = "fullName";
	public static final String KEY_USER_PASSWORD = "password";

	@Autowired
	private final ProfileDriverImpl profileDriver;

	@Autowired
	private final PlaylistDriverImpl playlistDriver;

	OkHttpClient client = new OkHttpClient();

	public ProfileController(ProfileDriverImpl profileDriver, PlaylistDriverImpl playlistDriver) {
		this.profileDriver = profileDriver;
		this.playlistDriver = playlistDriver;
	}

	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addProfile(@RequestParam Map<String, String> params,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		
		String userName = params.get("userName");
        String fullName = params.get("fullName");
        String password = params.get("password");
        // Check if userName, full name, or password is not null
        if(userName != null && fullName != null && password != null) {
        // Call the createUserProfile method and store it inside DbQueryStatus variable
          DbQueryStatus status = profileDriver.createUserProfile(userName, fullName, password); 
          Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
        }
        else {
          Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_GENERIC, null);
        }
        // Send the response back
        return response;

	}

	@RequestMapping(value = "/followFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> followFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		// Check that friendUserName and UserName are not null
		if(friendUserName != null && userName!=null) {
		  DbQueryStatus status = profileDriver.followFriend(userName, friendUserName);
		  // Set the responseStatus with the return values obtained from followFriend();
		  Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		}
		else {
		  Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_GENERIC, null);
		}
		// Send the response back
		return response;
	}

	@RequestMapping(value = "/getAllFriendFavouriteSongTitles/{userName}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getAllFriendFavouriteSongTitles(@PathVariable("userName") String userName,
			HttpServletRequest request) {

	  Map<String, Object> response = new HashMap<String, Object>();
      response.put("path", String.format("GET %s", Utils.getUrl(request)));
      // Check that the userName is provided
      if(userName != null) {
        // Call the getAllSongFriendsLike method in playilstDriverImpl
        DbQueryStatus status = profileDriver.getAllSongFriendsLike(userName); 
        Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
      }
      else {
        Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_GENERIC, null);
      }
      // Send the response back
      return response;
	}


	@RequestMapping(value = "/unfollowFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unfollowFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		  // Check if friendUserName and userName are provided
	      if(friendUserName != null && userName!=null) {
	        // Call the unfollowFriend method in the playlist driver class
	          DbQueryStatus status = profileDriver.unfollowFriend(userName, friendUserName);
	          Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
	        }
	        else {
	          Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_GENERIC, null);
	        }
	      // send the response back
	        return response;
	}

	@RequestMapping(value = "/likeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> likeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		// Check that username and songId are provided
		if(userName != null && songId != null) {
		  DbQueryStatus status = playlistDriver.likeSong(userName, songId);
		  // Set Utils to the values received back
		  Utils.setResponseStatus(response, status.getdbQueryExecResult(), (JSONObject) status.getData());
		}
		else {
		  Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_GENERIC, null);
		}
		// send the response back
		return response;
	}

	@RequestMapping(value = "/unlikeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unlikeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		// Check that userName and songId are provided
        if(userName != null && songId != null) {
          DbQueryStatus status = playlistDriver.unlikeSong(userName, songId);
          Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
        }
        else {
          // If path variables are not provided then return with an error code
          Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_GENERIC, null);
        }
     // Send the response back
        return response;
	}

	@RequestMapping(value = "/deleteAllSongsFromDb/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> deleteAllSongsFromDb(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		// Verify that the songId is provided
		if(songId != null) {
		  DbQueryStatus status = playlistDriver.deleteSongFromDb(songId);
		  Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		}else {
		  Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_GENERIC, null);
		}
		// Send the response back
		return response;
	}
}