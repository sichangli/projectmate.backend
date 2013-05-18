package projectmate.backend.datastore;

import java.util.Date;
import java.util.List;

import projectmate.backend.models.Project;
import projectmate.backend.models.Task;
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
	
	public void addProjectCaller(Project proj){
		addProject(proj);
	}
	
	private void addProject(Project proj) {
		Key keyforPair = KeyFactory.createKey("pair", "default");
		Key keyforProj = KeyFactory.createKey("proj", "default");
		
		List users = proj.getUsers();
		/*First setup multiple pairs of project and users*/
		long pid = proj.getProid();
		
		/*Should be no need to build task at this moment*/
		//List tasks = proj.getTasks();
		String owner = proj.getOwner();
		Date deadline = proj.getDeadline();
		String title = proj.getTitle();
		String desc = proj.getDescr();
		int status = proj.getStatus();
		Entity userpropair = null;
		/*make <proj, user> pair as entity for search projects for a user*/
		for(Object uid : users)
		{
			userpropair = new Entity("pair", keyforPair);
			uid = (String)uid;
			userpropair.setProperty("userid", uid);
			userpropair.setProperty("projid", pid);
			datastore.put(userpropair);
		}
		
		/*make proj entity*/
		Entity project = new Entity("proj", keyforProj);
		project.setProperty("title", title);
		project.setProperty("owner", owner);
		project.setProperty("desc", desc);
		project.setProperty("status", status);
		project.setProperty("deadline", deadline);
		project.setProperty("pid", pid);
		datastore.put(project);
		
	}
	
	public void createTask(Task task){
		String taskid = Long.toString(task.getTaskId());
		String projid = Long.toString(task.getParentProj());
		
		Key keyforPair = KeyFactory.createKey("taskpair", "default");
		Key keyforTask = KeyFactory.createKey("task", projid);
		
		long tid = task.getTaskId();
		long parentProj = task.getParentProj();
		String owner = task.getOwner();
		String desc = task.getDesc();
		String title = task.getTitle();
		Date deadline = task.getDeadline();
		List users = task.getUsers();
		int status = task.getStatus();
		
		/*Create a <proj, task> pair first*/
		for(Object user : users){
			Entity userpair = new Entity("taskpair", keyforPair);
			user = (String) user;
			userpair.setProperty("userid", user);
			userpair.setProperty("taskid", tid);
			datastore.put(userpair);
		}
		
		
		Entity taskentity = new Entity("task", keyforTask);
		taskentity.setProperty("tid", tid);
		taskentity.setProperty("owner", owner);
		taskentity.setProperty("desc", desc);
		taskentity.setProperty("deadline", deadline);
		taskentity.setProperty("status", status);
		taskentity.setProperty("parentProj", parentProj);
		taskentity.setProperty("title", title);
		
		datastore.put(taskentity);
	}
	
	
	
}
