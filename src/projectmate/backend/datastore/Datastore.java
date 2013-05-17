package projectmate.backend.datastore;

import java.util.List;

import projectmate.backend.models.User;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

public class Datastore {
	
	private DatastoreService datastore;

	public Datastore() {
		datastore = DatastoreServiceFactory.getDatastoreService();
	}
	
	public User logIn(String userId, String password) {
		Entity user = findUserEntity(userId);
		if (user != null) {
			String tempPassword = (String) user.getProperty("password");
			if (tempPassword.equals(password))
				return getUserFromEntity(user);
			else
				return null;
		}
		else
			return null;
	}
	
	private User getUserFromEntity(Entity userEntity) {
		User user = new User();
		user.setUserId((String) userEntity.getProperty("userId"));
		user.setPassword((String) userEntity.getProperty("password"));
		return user;
	}
	
	public boolean signup(String userId, String password) {
		Entity userEntity = findUserEntity(userId);
		if (userEntity != null)
			return false;
		else {
			addUser(userId, password);
			return true;
		}
	}
	
	private void addUser(String userId, String password) {
		Key key = KeyFactory.createKey("user", "default");
		Entity userE = new Entity("user", key);
		userE.setProperty("userId", userId);
		userE.setProperty("password", password);
		datastore.put(userE);
	}
	
	private Entity findUserEntity(String userId) {
		Entity user = null;
		Key key = KeyFactory.createKey("user", "default");
		Query query = new Query("user", key);
		List<Entity> users = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
		for (Entity u : users) {
			String tempUserId = (String) u.getProperty("userId");
			if (userId.equals(tempUserId))
				user = u;
		}
		return user;
	}
}
