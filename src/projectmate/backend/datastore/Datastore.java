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
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
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
		long status = proj.getStatus();
		
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
			userpropair.setProperty("fav", 0);
			Date currdate = new Date();
			long visitTime = currdate.getTime();
			userpropair.setProperty("visitTime", visitTime);
			datastore.put(userpropair);
		}
		
		return pid;
	}
	
	public String createTaskId(long projectId, Key keyforTask) {
		Filter taskFilter = new FilterPredicate("parentProj", FilterOperator.EQUAL, projectId);
		Query query = new Query("task", keyforTask).setFilter(taskFilter);
		List<Entity> list =  datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1000));
		int tid = list.size() + 1;
		return projectId + "-" + tid;
	}
	
	public void createTask(Task task){
		String projid = Long.toString(task.getParentProj());
		
		Key keyforPair = KeyFactory.createKey("taskpair", "default");
		Key keyforTask = KeyFactory.createKey("task", "default");
		
		long parentProj = task.getParentProj();
		String tid = createTaskId(parentProj, keyforTask);
		String owner = task.getOwner();
		String desc = task.getDesc();
		String title = task.getTitle();
		Date deadline = task.getDeadline();
		List users = task.getUsers();
		long status = task.getStatus();
		
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
	
	//get all tasks for one person
	public ArrayList<Task> findTasks(String userId) {
		ArrayList<String> tids = findAllTaskPairs(userId);
		ArrayList<Task> tasks = getTasks(tids);
		return tasks;
	}
	
	private ArrayList<String> findAllTaskPairs(String userId) {
		Key keyforPair = KeyFactory.createKey("taskpair", "default");
		Filter userFilter = new FilterPredicate("userid", FilterOperator.EQUAL, userId);
		Query query = new Query("taskpair", keyforPair).setFilter(userFilter);
		List<Entity> pairs = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
		ArrayList<String> result = new ArrayList<String> ();
		for (Entity pair : pairs) {
			String tmp = (String) pair.getProperty("taskid");
			result.add(tmp);
		}
		return result;
	}
	
	private ArrayList<Task> getTasks(ArrayList<String> tids) {
		Key keyforTask = KeyFactory.createKey("task", "default");
		ArrayList<Task> tasks = new ArrayList<Task> ();
		
		for (String tid : tids) {
			Filter taskFilter = new FilterPredicate("tid", FilterOperator.EQUAL, tid);
			Query query = new Query("task", keyforTask).setFilter(taskFilter);
			List<Entity> list = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
			Entity tmp = (Entity) list.get(0);
			Task task = new Task();
			task.setTaskId((String) tmp.getProperty("tid"));
			task.setOwner((String) tmp.getProperty("owner"));
			task.setDesc((String) tmp.getProperty("desc"));
			task.setDeadline((Date) tmp.getProperty("deadline"));
			task.setStatus((Long) tmp.getProperty("status"));
			task.setParentProj((Long) tmp.getProperty("parentProj"));
			task.setTitle((String) tmp.getProperty("title"));
			tasks.add(task);
		}
		return tasks;
	}
	
	/*Edit project*/
	private void editProject(int flag, Project proj){
		Key key = KeyFactory.createKey("pair", "default");
		
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
			tmpproj.setStatus((Long) tmp.getProperty("status"));
			tmpproj.setTitle((String) tmp.getProperty("title"));
			
			projects.add(tmpproj);
		}
		
		return projects;
	}
	
	//get all upcoming projects for a user
	public ArrayList<Project> getUpcomingProjects(String userId) {
		ArrayList<Project> all = findAllProjects(userId);
		ArrayList<Project> upcoming = new ArrayList<Project>();
		for (Project proj : all) {
			long status = proj.getStatus();
			Date deadline = proj.getDeadline();
			Date today = new Date();
			if (status == 0 && isUpcoming(today, deadline)) {
				upcoming.add(proj);
			}
		}
		return upcoming;
	}
	
	//check whether (deadline - 7  < today < deadline)
	private boolean isUpcoming(Date today, Date deadline) {
		long todayTime = today.getTime();
		long deadlineTime = deadline.getTime();
		long diffDays = (deadlineTime - todayTime) / (24 * 60 * 60 * 1000);
		if (today.before(deadline) && diffDays < 7) {
			return true;
		}
		else
			return false;
	}
	
	//get all on-going projects for a user
	public ArrayList<Project> getOngoingProjects(String userId) {
		ArrayList<Project> all = findAllProjects(userId);
		ArrayList<Project> ongoing = new ArrayList<Project>();
		for (Project proj : all) {
			long status = proj.getStatus();
			Date deadline = proj.getDeadline();
			Date today = new Date();
			if (proj.getStatus() == 0 && today.before(deadline))
				ongoing.add(proj);
		}
		return ongoing;
	}
	
	//get all completed projects for a user
	public ArrayList<Project> getCompletedProjects(String userId) {
		ArrayList<Project> all = findAllProjects(userId);
		ArrayList<Project> completed = new ArrayList<Project>();
		for (Project proj : all) {
			long status = proj.getStatus();
			if (status == 1)
				completed.add(proj);
		}
		return completed;
	}
	
	//get all favorite projects for a user
	public ArrayList<Project> getFavoriteProjects(String userId) {
		Key keyforPair = KeyFactory.createKey("pair", "default");
		Filter taskFilter = new FilterPredicate("userid", FilterOperator.EQUAL, userId);
		Filter favFilter = new FilterPredicate("fav", FilterOperator.EQUAL, 1);
		CompositeFilter comFilter = CompositeFilterOperator.and(taskFilter, favFilter);
		Query query = new Query("task", keyforPair).setFilter(taskFilter);
		List<Entity> list =  datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1000));
		ArrayList<Project> result = new ArrayList<Project> ();
		if(list == null){
			return result;
		} else {
			for(Entity proj : list){
				Project tmp = new Project();
				/*Need to fecth tasks here*/
				ArrayList<Long> tasks = new ArrayList<Long> ();
				
				
				tmp.setDeadline((Date)proj.getProperty("deadline"));
				tmp.setTitle((String)proj.getProperty("title"));
				tmp.setProid((Long)proj.getProperty("pid"));
				
			}
		}
	}
	
}
