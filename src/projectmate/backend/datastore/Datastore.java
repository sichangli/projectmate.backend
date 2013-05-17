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
	
	public User logIn(String name, String password) {
		Entity user = findUserEntity(name);
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
		user.setName((String) userEntity.getProperty("name"));
		user.setPassword((String) userEntity.getProperty("password"));
		return user;
	}
	
	public boolean signUp(User user) {
		Entity userEntity = findUserEntity(user.getName());
		if (userEntity != null)
			return false;
		else {
			addUser(user);
			return true;
		}
	}
	
	private void addUser(User user) {
		Key key = KeyFactory.createKey("user", "default");
		Entity userE = new Entity("user", key);
		userE.setProperty("name", user.getName());
		userE.setProperty("password", user.getPassword());
		datastore.put(userE);
	}
	
	private Entity findUserEntity(String name) {
		Entity user = null;
		Key key = KeyFactory.createKey("user", "default");
		Query query = new Query("user", key);
		List<Entity> users = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
		for (Entity u : users) {
			String tempName = (String) u.getProperty("name");
			if (name.equals(tempName))
				user = u;
		}
		return user;
	}
}
