package projectmate.backend.models;

import java.util.ArrayList;
import java.util.Date;

public class Project {
	private String title;
	private String descr;
	private long proid;
	private ArrayList<Long> tasks;
	private ArrayList<String> users;
	private Date deadline;
	private String owner;
	private int status;
	
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public ArrayList<String> getUsers() {
		return users;
	}
	public void setUsers(ArrayList<String> users) {
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
	public Date getDeadline() {
		return deadline;
	}
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	} 
}
