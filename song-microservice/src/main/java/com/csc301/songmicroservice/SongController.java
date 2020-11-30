package com.csc301.songmicroservice;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class SongController {

    @Autowired
    private final SongDal songDal;

    private OkHttpClient client = new OkHttpClient();

    
    public SongController(SongDal songDal) {
        this.songDal = songDal;
    }

    
    @RequestMapping(value = "/getSongById/{songId}", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> getSongById(@PathVariable("songId") String songId,
            HttpServletRequest request) {
      
      //guessing the implementation for this since not in handout 
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("path", String.format("GET %s", Utils.getUrl(request)));

        if(songId != null) {
          try { 
            //to check if the id is a valid id format 
            ObjectId temp = new ObjectId(songId);
            DbQueryStatus status = songDal.findSongById(songId);
            Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
          }
          catch(Exception e) { 
            Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_GENERIC, null);
          }
        }
        else {
          Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_GENERIC, null);
        }
        
        return response;
    }

    
    @RequestMapping(value = "/getSongTitleById/{songId}", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> getSongTitleById(@PathVariable("songId") String songId,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("path", String.format("GET %s", Utils.getUrl(request)));
        
        if(songId != null) {
          try { 
            //to check if the id is a valid id format
            ObjectId temp = new ObjectId(songId);
            DbQueryStatus status = songDal.getSongTitleById(songId);
            Utils.setResponseStatus(response, status.getdbQueryExecResult(), ((Song) status.getData()).getSongName());
          }
          catch(Exception e){ 
            Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_GENERIC, null);
          }
        }
        else {
          Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_GENERIC, null);
        }
        
        return response;
    }
// Here
    
    @RequestMapping(value = "/deleteSongById/{songId}", method = RequestMethod.DELETE)
    public @ResponseBody Map<String, Object> deleteSongById(@PathVariable("songId") String songId,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("path", String.format("DELETE %s", Utils.getUrl(request)));

        DbQueryStatus status = songDal.deleteSongById(songId);
        
        Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
        
        return response;        
    }

    
    @RequestMapping(value = "/addSong", method = RequestMethod.POST)
    public @ResponseBody Map<String, Object> addSong(@RequestParam Map<String, String> params,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("path", String.format("POST %s", Utils.getUrl(request)));

        String songName = params.get("songName");
        String songArtistFullName = params.get("songArtistFullName");
        String songAlbum = params.get("songAlbum");
        
        if(songName != null && songArtistFullName != null && songAlbum != null) {
          Song song = new Song(songName, songArtistFullName, songAlbum);
          DbQueryStatus status = songDal.addSong(song);
          Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
        }
        else {
          Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_GENERIC, null);
        }
        
        return response;
    }

    
    @RequestMapping(value = "/updateSongFavouritesCount/{songId}", method = RequestMethod.PUT)
    public @ResponseBody Map<String, Object> updateFavouritesCount(@PathVariable("songId") String songId,
            @RequestParam("shouldDecrement") String shouldDecrement, HttpServletRequest request) {

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("data", String.format("PUT %s", Utils.getUrl(request)));
        
        boolean updateDecrement = Boolean.parseBoolean(shouldDecrement);
        
        DbQueryStatus status = songDal.updateSongFavouritesCount(songId, updateDecrement);
        
        Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
        
        return response;
    
    }
}