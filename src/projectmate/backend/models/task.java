package projectmate.backend.models;

import java.util.ArrayList;
import java.util.Date;

public class Task {
	private String title;
	private int status;
	private ArrayList<String> users;
	private long parentProj;
	private String owner;
	private String desc;
	private long taskId;
	private Date deadline;
	
	public String getTitle() {
		/*title for project*/
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public long getParentProj() {
		return parentProj;
	} 
	public void setParentProj(long parentProj) {
		this.parentProj = parentProj;
	}
	
	public ArrayList<String> getUsers() {
		return users;
	}
	public void setUsers(ArrayList<String> users) {
		this.users = users;
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	public Date getDeadline() {
		return deadline;
	}
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	
}
