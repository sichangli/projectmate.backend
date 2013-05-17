package projectmate.backend.models;

import java.util.ArrayList;
import java.util.Date;

public class project {
	private String title;
	private String descr;
	private long proid;
	private ArrayList<Long> tasks;
	private ArrayList<Long> users;
	private Date deadline;
	private long owner;
	private int status;
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public ArrayList<Long> getUsers() {
		return users;
	}
	public void setUsers(ArrayList<Long> users) {
		this.users = users;
	}
	public ArrayList<Long> getTasks() {
		return tasks;
	}
	public void setTasks(ArrayList<Long> tasks) {
		this.tasks = tasks;
	}
	public long getProid() {
		return proid;
	}
	public void setProid(long proid) {
		this.proid = proid;
	}
	public String getDescr() {
		return descr;
	}
	public void setDescr(String descr) {
		this.descr = descr;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	} 
}
