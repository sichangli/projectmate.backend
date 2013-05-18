package projectmate.backend.models;

import java.util.ArrayList;

public class Task {
	private String title;
	private int status;
	private ArrayList<Long> users;
	private long parentProj;
	private long owner;
	private String desc;
	
	public String getTitle() {
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
	
	public long getOwner() {
		return owner;
	}
	public void setOwner(long owner) {
		this.owner = owner;
	}
	
	public long getParentProj() {
		return parentProj;
	}
	public void setParentProj(long parentProj) {
		this.parentProj = parentProj;
	}
	
	public ArrayList<Long> getUsers() {
		return users;
	}
	public void setUsers(ArrayList<Long> users) {
		this.users = users;
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	
	
}
