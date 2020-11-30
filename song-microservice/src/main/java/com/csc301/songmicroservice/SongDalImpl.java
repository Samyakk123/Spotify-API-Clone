package com.csc301.songmicroservice;

import java.util.Map;
import org.bson.BasicBSONObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

@Repository
public class SongDalImpl implements SongDal {

    private final MongoTemplate db;
    private DbQueryStatus toReturn = null;

    @Autowired
    public SongDalImpl(MongoTemplate mongoTemplate) {
        this.db = mongoTemplate;
    }

    @Override
    public DbQueryStatus addSong(Song songToAdd) {
        // TODO Auto-generated method stub
//      Map<String, String> temp = songToAdd.getJsonRepresentation();
      toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
      
      try {
        db.insert(songToAdd, "songs");
        toReturn.setData(songToAdd);
        return toReturn;
      }catch(Exception e) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
        return toReturn;
      }
      
    }

    @Override
    public DbQueryStatus findSongById(String songId) {
      toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
      try {
        Song found = db.findById(songId, Song.class);
        if(found == null) { 
          toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND); 
        }
        toReturn.setData(found);
        return toReturn;
      }catch(Exception e) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
        return toReturn;
      }
    }

    @Override
    public DbQueryStatus getSongTitleById(String songId) {
        // TODO Auto-generated method stub
      toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
      try {
        Song found = db.findById(songId, Song.class);
        if(found == null) { 
          toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND); 
        }
        toReturn.setData(found);
        return toReturn;
      }catch(Exception e) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
        return toReturn;
      }
    }

    @Override
    public DbQueryStatus deleteSongById(String songId) {
        // TODO Auto-generated method stub
      toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);

      
      try {
        ObjectId temp = new ObjectId(songId);
        db.getCollection("songs").deleteOne(Filters.eq("_id", temp));
        toReturn.setData("OK");
      }catch(Exception e) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
      }
      return toReturn;
    }

    @Override
    public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {

      toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
      
      DbQueryStatus status = this.findSongById(songId);
      Song returnVal = (Song) status.getData();

      
      long currentVal = returnVal.getSongAmountFavourites();
      long increment = 0;
      if(shouldDecrement) {
        increment = -1;
      }
      else {
        increment = 1;
      }
      returnVal.setSongAmountFavourites(currentVal + increment);

      try {
        
        // FIX INVALID INPUT CHECK HERE :( DON'T RMMR HOW DIVYAM DID IT and too sleepy
        ObjectId temp = new ObjectId(songId);
        db.getCollection("songs").updateOne(Filters.eq("_id", temp), Updates.set("songAmountFavourites", currentVal+increment));
        toReturn.setData(returnVal);
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
      }
      catch(Exception e) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
      }
      
      return toReturn;
      
      
//      DbQueryStatus status = songDal.findSongById(songId);
//      Song returnVal = (Song) status.getData();
//      
//      long currentVal = returnVal.getSongAmountFavourites();
//      
//      long increment = Integer.parseInt(shouldDecrement);
//
//      returnVal.setSongAmountFavourites(currentVal + increment);
      
    }
}