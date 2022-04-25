import java.text.DecimalFormat;

public class Team {
	
	private String Name;
	private String Short;
	private int WorldRank;
	private String Group;
	private int GPlayed;
	private int GWon;
	private int GDrawn;
	private int GLost;
	private int GoalsScored;
	private int GoalsConceded;
	private int GoalDiff;
	private int TotalPoints;
	private int TimesWon;
	private int ETWon;
	private int ETDrawn;
	private int ETLost;
	private int PenWon;
	private int PenLost;
	private int TeamMorale;
	private int qualiMP;
	private int qualiGS;
	private int qualiGC;
	private double avgCards;
	private double xGf;
	private double xGa;
	private int predictionRisk;
  
	public Team(String n, String s, int wR, String g, int gP, int gW, int gD, int gL, int gS, int gC, int gDiff, int tP, int tW, int etW, int etD,
			int etL, int pW, int pL, int tM, double aGS, double aGC, int mP, int qGS, int qGC, double aC, double xgF, double xgA, int pRisk) {
		Name = n;
		Short = s;
		WorldRank = wR;
		Group = g;
		GPlayed = gP;
		GWon = gW;
		GDrawn = gD;
		GLost = gL;
		GoalsScored = gS;
		GoalsConceded = gC;
		GoalDiff = gDiff;
		TotalPoints = tP;
		TimesWon = tW;
		ETWon = etW;
		ETDrawn = etD;
		ETLost = etL;
		PenWon = pW;
		PenLost = pL;
		TeamMorale = tM;
		qualiMP = mP;
		qualiGS = qGS;
		qualiGC = qGC;
		avgCards = aC;
		xGf = xgF;
		xGa = xgA;
		predictionRisk = pRisk;
	}
	
	public String toString() {
		return Name + Short + WorldRank + Group + GPlayed + GWon + GDrawn + GLost + GoalsScored + GoalsConceded + GoalDiff + TotalPoints + TimesWon + ETWon + ETDrawn + ETLost + PenWon
				+ PenLost + TeamMorale;
	}
	
	public String getName() {
		return Name;
	}
	
	public void setName(String newName) {
		this.Name = newName;	
	}
  
	public String getShort() {
		return Short;
	}

	public void setShort(String newShort) {
		this.Short = newShort;
	}

	public int getWorldRank() {
		return WorldRank;
	}

	public void setWorldRank(int newWorldRank) {
		this.WorldRank = newWorldRank;
	}

	public String getGroup() {
		return Group;
	}

	public void setGroup(String newGroup) {
		this.Group = newGroup;
	}

	public int getGPlayed() {
		return GPlayed;
	}

	public void setGPlayed(int newGPlayed) {
		this.GPlayed = newGPlayed;
	}

	public int getGWon() {
		return GWon;
	}

	public void setGWon(int newGWon) {
		this.GWon = newGWon;
	}

	public int getGDrawn() {
		return GDrawn;
	}

	public void setGDrawn(int newGDrawn) {
		this.GDrawn = newGDrawn;
	}

	public int getGLost() {
		return GLost;
	}

	public void setGLost(int newGLost) {
		this.GLost = newGLost;
	}

	public int getGoalsScored() {
		return GoalsScored;
	}

	public void setGoalsScored(int newGoalsScored) {
		this.GoalsScored = newGoalsScored;
	}

	public int getGoalsConceded() {
		return GoalsConceded;
	}

	public void setGoalsConceded(int newGoalsConceded) {
		this.GoalsConceded = newGoalsConceded;
	}

	public int getGoalDiff() {
		return GoalDiff;
	}

	public void setGoalDiff(int newGoalDiff) {
		this.GoalDiff = newGoalDiff;
	}

	public int getTotalPoints() {
		return TotalPoints;
	}

	public void setTotalPoints(int newTotalPoints) {
		this.TotalPoints = newTotalPoints;
	}

	public int getTimesWon() {
		return TimesWon;
	}

	public void setTimesWon(int newTimesWon) {
		this.TimesWon = newTimesWon;
	}
  
	public int getETWon() {
		return ETWon;
	}
  
	public void setETWon(int newETWon) {
		this.ETWon = newETWon;
	}
  
	public int getETDrawn() {
		return ETDrawn;
	}
  
	public void setETDrawn(int newETDrawn) {
		this.ETDrawn = newETDrawn;
	}
  
	public int getETLost() {
		return ETLost;
	}
  
	public void setETLost(int newETLost) {
		this.ETLost = newETLost;
	}
  
	public int getPenWon() {
		return PenWon;
	}
  
	public void setPenWon(int newPenWon) {
		this.PenWon = newPenWon;
	}
  
	public int getPenLost() {
		return PenLost;
	}
  
	public void setPenLost(int newPenLost) {
		this.PenLost = newPenLost;
	}
  
	public int getTeamMorale() {
		return TeamMorale;
	}
  
	public void setTeamMorale(int newTeamMorale) {
		this.TeamMorale = newTeamMorale;
	}
	
	public double getAvgGS() {
		DecimalFormat df = new DecimalFormat("0.00");
		return Double.parseDouble(df.format((this.getGoalsScored() + (double) this.getQualiGS())/(this.getGPlayed() + (double) this.getQualiMP())));
	}
	
	public double getAvgGC() {
		DecimalFormat df = new DecimalFormat("0.00");
		return Double.parseDouble(df.format((this.getGoalsConceded() + (double) this.getQualiGC())/(this.getGPlayed() + (double) this.getQualiMP())));
	}
	
	public int getQualiMP() {
		return qualiMP;
	}
	
	public void setQualiMP(int newMP) {
		this.qualiMP = newMP;	
	}
	
	public int getQualiGS() {
		return qualiGS;
	}
	
	public void setQualiGS(int newGS) {
		this.qualiGS = newGS;	
	}
	
	public int getQualiGC() {
		return qualiGC;
	}
	
	public void setQualiGC(int newGC) {
		this.qualiGC = newGC;	
	}
	
	public double getAvgC() {
		return avgCards;
	}
	
	public void setAvgC(double newAC) {
		this.avgCards = newAC;	
	}
	
	public double getXGF() {
		return xGf;
	}
	
	public void setXGF(double newXGF) {
		this.xGf = newXGF;	
	}
	
	public double getXGA() {
		return xGa;
	}
	
	public void setXGA(double newXGA) {
		this.xGa = newXGA;	
	}
	
	public int getPredRisk() {
		return predictionRisk;
	}
	
	public void setPredRisk(int newPR) {
		this.predictionRisk = newPR;	
	}
}