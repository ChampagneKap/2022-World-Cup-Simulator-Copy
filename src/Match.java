public class Match {
	
	private int matchID;
	private String hTeam;
	private int hTeamGoals;
	private int aTeamGoals;
	private String aTeam;
	private String group;
	private int hTeamETGoals;
	private int aTeamETGoals;
	private int hTeamPens;
	private int aTeamPens;
	
	public Match(int mID, String hT, int hTgoals, int aTgoals, String aT, String g, int hTETgoals, int aTETgoals, int hTpens, int aTpens) {
		matchID = mID;
		hTeam = hT;
		hTeamGoals = hTgoals;
		aTeamGoals = aTgoals;
		aTeam = aT;
		group = g;
		hTeamETGoals = hTETgoals;
		aTeamETGoals = aTETgoals;
		hTeamPens = hTpens;
		aTeamPens = aTpens;
	}
	
	public int getMatchID() {
	  	return matchID;
	}

	public void setMatchID(int mID) {
		this.matchID = mID;
	}
	  
	public String gethTeam() {
	  	return hTeam;
	}

	public void sethTeam(String hT) {
		this.hTeam = hT;
	}

	public int gethTeamGoals() {
		return hTeamGoals;
	}

	public void sethTeamGoals(int hTg) {
	  	this.hTeamGoals = hTg;
	}
	
	public int getaTeamGoals() {
		return aTeamGoals;
	}

	public void setaTeamGoals(int aTg) {
	  	this.aTeamGoals = aTg;
	}

	public String getaTeam() {
	  	return aTeam;
	}

	public void setaTeam(String aT) {
		this.aTeam = aT;
	}
	
	public String getGroup() {
	  	return group;
	}

	public void setGroup(String newGroup) {
	  	this.group = newGroup;
	}
	
	public int gethTeamETGoals() {
		return hTeamETGoals;
	}

	public void sethTeamETGoals(int hTETg) {
	  	this.hTeamETGoals = hTETg;
	}
	
	public int getaTeamETGoals() {
		return aTeamETGoals;
	}

	public void setaTeamETGoals(int aTETg) {
	  	this.aTeamETGoals = aTETg;
	}
	
	public int gethTeamPens() {
		return hTeamPens;
	}

	public void sethTeamPens(int hTp) {
	  	this.hTeamPens = hTp;
	}
	
	public int getaTeamPens() {
		return aTeamPens;
	}

	public void setaTeamPens(int aTp) {
	  	this.aTeamPens = aTp;
	}
}