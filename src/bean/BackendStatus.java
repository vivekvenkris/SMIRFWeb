package bean;

import util.BackendConstants;

public class BackendStatus{

	public boolean on;
	public String state;
	public String response;
	
	@Override
	public String toString() {
		return (on? "ON" : "OFF") +  " and " + state + (on ? "" : response);
	}
	
	public BackendStatus(boolean on, String state, String response) {
		this.on = on;
		this.state = state;
		this.response = response;
	}
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	
	public boolean isRecording(){
		return state.equalsIgnoreCase("Recording");
	}
	
	public boolean isIdle(){
		return state.equalsIgnoreCase("Idle");
	}
	
	public boolean isPrepared(){
		return state.equalsIgnoreCase("Prepared");
	}
	
	public boolean isPreparing(){
		return state.equalsIgnoreCase("Preparing");
	}
	
	public boolean isStarting(){
		return state.equalsIgnoreCase("Starting");
	}
	
	public boolean isStopping(){
		return state.equalsIgnoreCase("Stopping");
	}
	
	public boolean isFunctional(){
		return !( isRecording() || isIdle() || isPrepared() || isPreparing() || isStarting() || isStopping());
	}
	
	public boolean hasFailed(){
		return !isFunctional();
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}
	
	
}
