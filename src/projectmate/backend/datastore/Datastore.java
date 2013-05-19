package projectmate.backend.datastore;

import java.util.ArrayList;
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
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

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
		user.setLastName((String) userEntity.getProperty("lastName"));
		user.setFirstName((String) userEntity.getProperty("firstName"));
		user.setSex((String) userEntity.getProperty("sex"));
		return user;
	}
	
	public boolean signup(User user) {
		Entity userEntity = findUserEntity(user.getUserId());
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
		userE.setProperty("userId", user.getUserId());
		userE.setProperty("password", user.getPassword());
		userE.setProperty("lastName", user.getLastName());
		userE.setProperty("firstName", user.getFirstName());
		userE.setProperty("sex", user.getSex());
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
	
	public long addProjectCaller(Project proj){
		long pid = addProject(proj);
		return pid;
	}
	
	private Long addPid() {
		Key key = KeyFactory.createKey("proj", "default");
		Query q = new Query("proj", key);
		List<Entity> list =  datastore.prepare(q).asList(FetchOptions.Builder.withLimit(1000));
		if(list == null)
			return new Long(1);
		int res = list.size();
		res++;
		return new Long(res);
	}
	
	private long addProject(Project proj) {
		Key keyforPair = KeyFactory.createKey("pair", "default");
		Key keyforProj = KeyFactory.createKey("proj", "default");
		
		List users = proj.getUsers();
		
		/*Should be no need to build task at this moment*/
		//List tasks = proj.getTasks();
		String owner = proj.getOwner();
		Date deadline = proj.getDeadline();
		String title = proj.getTitle();
		String desc = proj.getDescr();
		int status = proj.getStatus();
		
		
		/*make proj entity*/
		Entity project = new Entity("proj", keyforProj);
		project.setProperty("title", title);
		project.setProperty("owner", owner);
		project.setProperty("desc", desc);
		project.setProperty("status", status);
		project.setProperty("deadline", deadline);
		long pid = addPid();
		project.setProperty("pid", pid);
		datastore.put(project);
		
		/*make <proj, user> pair as entity for search projects for a user*/
		Entity userpropair = null;
		for(Object uid : users)
		{
			userpropair = new Entity("pair", keyforPair);
			uid = (String)uid;
			userpropair.setProperty("userid", uid);
			userpropair.setProperty("projid", pid);
			datastore.put(userpropair);
		}
		
		return pid;
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
		
		/*Create a <user, task> pair first*/
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
	
	/*Edit project*/
	private void editProject(int flag, Project proj){
		
		
	}
	
	/*Get all projects for one person*/
	public ArrayList<Project> findAllProjects(String userid){
		ArrayList<Long> pids = findAllPairs(userid);
		ArrayList<Project> projs = getProjects(pids);
		return projs;
	}
	
	private ArrayList<Long> findAllPairs(String userid){
		Key key = KeyFactory.createKey("pair", "default");
		
		Filter userFilter = new FilterPredicate("userid", FilterOperator.EQUAL, userid);
		Query query = new Query("pair", key).setFilter(userFilter);
		List<Entity> pairs = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
		ArrayList<Long> result = new ArrayList<Long> ();
		for(Entity pair : pairs){
			Long tmp = (Long) pair.getProperty("pid");
			result.add(tmp);
		}
		
		return result;
	}
	
	private ArrayList<Project> getProjects(ArrayList<Long> pids){
		Key key = KeyFactory.createKey("proj", "default");
		ArrayList<Project> projects = new ArrayList<Project> ();
		for(Long pid : pids){
			Filter projFilter = new FilterPredicate("pid", FilterOperator.EQUAL, Long.valueOf(pid));
			Query query = new Query("proj", key);
			List<Entity> list = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
			Entity tmp = (Entity) list.get(0);
			Project tmpproj = new Project();
			tmpproj.setDeadline((Date) tmp.getProperty("deadline"));
			tmpproj.setDescr((String) tmp.getProperty("desc"));
			tmpproj.setOwner((String) tmp.getProperty("owner"));
			tmpproj.setProid((Long) tmp.getProperty("pid"));
			tmpproj.setStatus((Integer) tmp.getProperty("status"));
			tmpproj.setTitle((String) tmp.getProperty("title"));
			
			projects.add(tmpproj);
		}
		
		return projects;
	}
	
}
