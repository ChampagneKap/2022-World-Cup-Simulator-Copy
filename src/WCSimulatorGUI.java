import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.*;
import java.security.SecureRandom;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import javax.sound.sampled.*;

class WCSimulatorGUI {
	
	// set of global variables necessary for the application
	String userTeamString, mode, fileName;
	Team userTeam;
	List<Team> teams, progressed, WCfinal;
	List<String> playedAgainst, stadiums;
	List<List<Team>> allGroups, allR16s, allQFs, allSFs;
	List<List<String>> allGroupsString;
	int groupCount = 0, matches = 0, KOmatches = -1, KOcount = 0, KOlimit = 7, noOfSims, userRating;
	boolean userTeamProgressed, continueSim, overwrite, userPlayedKOgame;
	
	// global variables for the GUI elements
	JFrame window;
    Container con;
    JPanel gsGameResultsPanel, koGamesResults, endPanel;
    JLabel simmingTournamentLabel, exitLabel;
    JTextField getUserInput;
    JSlider slider;
    Clip clip, buttonClip;
	Font titleFont, textFont, buttonFont, statFont, userTeamFont;
    Border border1 = BorderFactory.createMatteBorder(-1, -1, 3, 3, Color.white);
    Border border2 = BorderFactory.createMatteBorder(-1, -1, 3, -1, Color.white);
    Border border3 = BorderFactory.createMatteBorder(-1, -1, -1, 3, Color.white);
    
	ChoiceHandler choiceHandler = new ChoiceHandler();
	SliderChanger sliderChanger = new SliderChanger();
	
	// listener for changing the value on the slider 
	class SliderChanger implements ChangeListener {

		public void stateChanged(ChangeEvent e) {
			getUserInput.setText("" + slider.getValue());
		}
		
	}
	
	// handler for all the buttons a user can choose throughout the application
	class ChoiceHandler implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			playSound("buttonClick.wav");
			 
			switch (e.getActionCommand()) {
				// cases for the title screen choices
				case "classicMode":
					mode = "classic";
					classicModeTable(allGroupsString, 0);
					break;
				case "loadGame":
					checkingSaveFiles();
					break;
				case "simMode":
					mode = "sim";
					gettingNoOfSims();
					break;
				// cases for the classic mode table, choosing a team
				case "simGS":
					simulatingGroupStages(false);
					break;
				case "chooseAgain":
					classicModeTable(allGroupsString, 0);
					break;
				default:
					userTeamString = e.getActionCommand();
					for (Team t : teams)
						if (t.getName().equals(e.getActionCommand())) userTeam = t;
					confirmTeamSelection();
					break;
				// cases for the rest of the application
				case "playGSGame":
					if (clip != null) clip.stop();
					playGSGame();
					break;
				case "saveGame":
					chooseSaveFile();
					break;
				case "nextGroup":
					groupCount++;
					if (groupCount < 8) simulateCMGroups(allGroups.get(groupCount));
					return;
				case "previousGroup":
					groupCount--;
					if (groupCount > -1) simulateCMGroups(allGroups.get(groupCount));
					return;
				case "checkGroup":
					if (clip != null) {
						clip.stop();
						playSound("mainMenuMusic.wav");
					}
					simulateCMGroups(allGroups.get(groupCount));
					break;
				case "checkProgressed":
					checkUserTeamProgressed();
					break;
				case "nextStage":
					if (KOmatches == 3 && progressed.get(30).getName().equals(userTeam.getName())) winninglosingWC(true);
					else if (KOmatches == 3 && !progressed.get(30).getName().equals(userTeam.getName())) winninglosingWC(false);
					else {
						KOmatches++;
						if (KOmatches == 1) KOlimit = 3;
						else if (KOmatches == 2) KOlimit = 1;
						simulatingKnockoutStages(false);
					}
					break;
				case "playCMAgain":
					playCMwithSameTeam();
					break;
				case "sameTeam":
					if (clip != null) clip.stop();
					mainMenu(2);
					break;
				case "diffTeam":
					if (clip != null) clip.stop();
					mainMenu(1);
					break;
				case "mainMenu":
					//if (clip != null) clip.stop();
					mainMenu(0);
					break;
				case "nextGame":
					if (clip != null && userPlayedKOgame) {
						clip.stop();
						playSound("mainMenuMusic.wav");
					}
					KOcount++;
					if (KOmatches == 0)
						if (KOcount < 8) backgroundKnockoutGames(allR16s.get(KOcount));
					if (KOmatches == 1)
						if (KOcount < 4) backgroundKnockoutGames(allQFs.get(KOcount));
					if (KOmatches == 2)
						if (KOcount < 2) backgroundKnockoutGames(allSFs.get(KOcount));
					break;
				case "previousGame":
					if (clip != null && userPlayedKOgame) {
						clip.stop();
						playSound("mainMenuMusic.wav");
					}
					KOcount--;
					if (KOmatches == 0)
						if (KOcount > -1) backgroundKnockoutGames(allR16s.get(KOcount));
					if (KOmatches == 1)
						if (KOcount > -1) backgroundKnockoutGames(allQFs.get(KOcount));
					if (KOmatches == 2)
						if (KOcount > -1) backgroundKnockoutGames(allSFs.get(KOcount));
					break;
				case "playKOGame":
					if (clip != null) clip.stop();
					if (KOmatches == 0) playKOGame(allR16s.get(KOcount).get(0).getName(), allR16s.get(KOcount).get(1).getName());
					else if (KOmatches == 1) playKOGame(allQFs.get(KOcount).get(0).getName(), allQFs.get(KOcount).get(1).getName());
					else if (KOmatches == 2) playKOGame(allSFs.get(KOcount).get(0).getName(), allSFs.get(KOcount).get(1).getName());
					else if (KOmatches == 3) playKOGame(WCfinal.get(0).getName(), WCfinal.get(1).getName());
					break;
				case "simTest":
					noOfSims = Integer.parseInt(getUserInput.getText());
					simulatingTournament();
					break;
				case "simAgain":
					gettingNoOfSims();
					break;
				case "rateSim":
					gettingUserRating();
					break;
				case "feedback":
					userRating = Integer.parseInt(getUserInput.getText());
					finishSimulation();
					break;
				case "resetSlider":
					getUserInput.setText("5");
					slider.setValue(5);
					break;
				case "resetSlider2":
					getUserInput.setText("0");
					slider.setValue(0);
					break;
				case "finishSim":
					continueSim = true;
					if (KOmatches == 3 && progressed.get(30).getName().equals(userTeam.getName())) winninglosingWC(true);
					else if (KOmatches == 3 && !progressed.get(30).getName().equals(userTeam.getName())) winninglosingWC(false);
					else {
						KOmatches++;
						if (KOmatches == 1) KOlimit = 3;
						else if (KOmatches == 2) KOlimit = 1;
						simulatingKnockoutStages(false);
					}
					break;
				case "afterSave":
					loadingGame();
					break;
				case "saveFile1":
					fileName = "savedGame1.txt";
					saveGame(false);
					break;
				case "saveFile2":
					fileName = "savedGame2.txt";
					saveGame(false);
					break;
				case "saveFile3":
					fileName = "savedGame3.txt";
					saveGame(false);
					break;
				case "loadFile1":
					fileName = "savedGame1.txt";
					checkingForSavedGame();
					break;
				case "loadFile2":
					fileName = "savedGame2.txt";
					checkingForSavedGame();
					break;
				case "loadFile3":
					fileName = "savedGame3.txt";
					checkingForSavedGame();
					break;
				case "overwriteYes":
					saveGame(true);
					break;
				case "overwriteNo":
					chooseSaveFile();
					break;
				case "checkStats":
					if (clip != null) {
						clip.stop();
						playSound("mainMenuMusic.wav");
					}
					statsPage(exitLabel);
					break;
				case "stats":
					displayingSimWinners(0, false);
					break;
				case "gameStats":
					displayingSimWinners(1, false);
					break;
				case "goalsStats":
					displayingSimWinners(2, false);
					break;
				case "etStats":
					displayingSimWinners(3, false);
					break;
				case "penStats":
					displayingSimWinners(4, false);
					break;
				case "statsReverse":
					displayingSimWinners(0, true);
					break;
				case "gameStatsReverse":
					displayingSimWinners(1, true);
					break;
				case "goalsStatsReverse":
					displayingSimWinners(2, true);
					break;
				case "etStatsReverse":
					displayingSimWinners(3, true);
					break;
				case "penStatsReverse":
					displayingSimWinners(4, true);
					break;
				case "gameResults":
					displayGroupGameResults(allGroups.get(groupCount));
					break;
				case "groupTable":
					simulateCMGroups(allGroups.get(groupCount));
					break;
				case "exit":
					System.exit(0);
					break;
			}
		}
	}
	
	// establish connection to sqlite database file
	public Connection connect() {
		String url = "jdbc:sqlite:" + this.getClass().getResource("Teams.db").getPath();
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			errorScreen(e.getMessage());
		}
		return conn;
	}
	
	// clear both the tables in the database file
	public void clearDatabase() {
		clearTeams();
		clearMatches();
	}
	
	// clear the Teams table in the database file
	public void clearTeams() {
		String query = "DELETE FROM WCTeams";
		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.executeUpdate();
		} catch (SQLException e) {
			createDatabaseFile();
		}
	}
	
	// clear the Matches table in the database file
	public void clearMatches() {
		String query = "DELETE FROM WCMatches";
		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.executeUpdate();
		} catch (SQLException e) {
			createDatabaseFile();
		}
	}
	
	// create a new database file with new tables, if the old file has been deleted/moved
	private void createDatabaseFile() {
		File f = new File("Teams.db");
		try {
			f.createNewFile();
			createTeamsTable();
			createMatchTable();
		} catch (IOException e1) {
			errorScreen(e1.getMessage());
		}
	}
	
	// create a new Teams table in the database file
	public void createTeamsTable() {
		String query = "CREATE TABLE WCTeams (`Name` TEXT, `Short` TEXT, `WorldRanking` INTEGER, `Group` TEXT, `GamesPlayed` INTEGER DEFAULT 0, `GamesWon` INTEGER DEFAULT 0, `GamesDrawn` INTEGER DEFAULT 0, `GamesLost` INTEGER DEFAULT 0, `GoalsScored` INTEGER DEFAULT 0, `GoalsConceded` INTEGER DEFAULT 0, `GoalDifference` INTEGER DEFAULT 0, `TotalPoints` INTEGER DEFAULT 0, `TimesWon` INTEGER DEFAULT 0, `ETWon` INTEGER DEFAULT 0, `ETDrawn` INTEGER DEFAULT 0, `ETLost` INTEGER DEFAULT 0, `PenWon` INTEGER DEFAULT 0, `PenLost` INTEGER DEFAULT 0, `TeamMorale` INTEGER DEFAULT 50, `AvgGS` DECIMAL(3,2), `AvgGC` DECIMAL(3,2), `QMP` INTEGER, `QGS` INTEGER, `QGC` INTEGER, `avgCards` DECIMAL(3,2), `xGf` DECIMAL(3,2), `xGa` DECIMAL(3,2), `predRisk` INTEGER, PRIMARY KEY(`Name`))";
		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.executeUpdate();
		} catch (SQLException e) {
			errorScreen(e.getMessage());
		}
	}
	
	// create a new Matches table in the database file
	public void createMatchTable() {
		String query = "CREATE TABLE WCMatches (`MatchID` INTEGER DEFAULT ROWID, `hTeam` TEXT, `hTeamGoals` INTEGER, `aTeamGoals` INTEGER, `aTeam` TEXT, `Group` TEXT, `hTeamETGoals` INTEGER DEFAULT 0, `aTeamETGoals` INTEGER DEFAULT 0, `hTeamPens` INTEGER DEFAULT 0, `aTeamPens` INTEGER DEFAULT 0, PRIMARY KEY(`MatchID`))";
		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.executeUpdate();
		} catch (SQLException e) {
			errorScreen(e.getMessage());
		}
	}
	
	// get world ranking of each team participating in the 2022 World Cup and insert each team into the database
	public void webScrape() {
		try {
			List<String> groupA = Arrays.asList("Qatar", "Ecuador", "Senegal", "Netherlands");
			List<String> groupB = Arrays.asList("England", "IR Iran", "USA", "Wales");
			List<String> groupC = Arrays.asList("Argentina", "Saudi Arabia", "Mexico", "Poland");
			List<String> groupD = Arrays.asList("France", "Peru", "Denmark", "Tunisia");
			List<String> groupE = Arrays.asList("Spain", "Costa Rica", "Germany", "Japan");
			List<String> groupF = Arrays.asList("Belgium", "Canada", "Morocco", "Croatia");
			List<String> groupG = Arrays.asList("Brazil", "Serbia", "Switzerland", "Cameroon");
			List<String> groupH = Arrays.asList("Portugal", "Ghana", "Uruguay", "Korea Republic");
			int count = 1;
			List<String> teamNames = new ArrayList<String>();
			
			String url = "http://en.fifaranking.net/ranking/";
			Document doc = Jsoup.connect(url).get();
			Elements tn = doc.select("table.table.table-striped.table-condensed td.nation a");
			
			for (Element dn : tn)
				teamNames.add(dn.text());
			
			for (String name : teamNames) {
				String[] teamDataTokens = parseCSVData(name);
				if (groupA.contains(name)) insertData(name, count, "A", Double.parseDouble(teamDataTokens[1]), Double.parseDouble(teamDataTokens[2]), Integer.parseInt(teamDataTokens[3]), Integer.parseInt(teamDataTokens[4]), Integer.parseInt(teamDataTokens[5]), Double.parseDouble(teamDataTokens[6]), Double.parseDouble(teamDataTokens[7]), Double.parseDouble(teamDataTokens[8]), Integer.parseInt(teamDataTokens[9]));
				if (groupB.contains(name)) insertData(name, count, "B", Double.parseDouble(teamDataTokens[1]), Double.parseDouble(teamDataTokens[2]), Integer.parseInt(teamDataTokens[3]), Integer.parseInt(teamDataTokens[4]), Integer.parseInt(teamDataTokens[5]), Double.parseDouble(teamDataTokens[6]), Double.parseDouble(teamDataTokens[7]), Double.parseDouble(teamDataTokens[8]), Integer.parseInt(teamDataTokens[9]));
				if (groupC.contains(name)) insertData(name, count, "C", Double.parseDouble(teamDataTokens[1]), Double.parseDouble(teamDataTokens[2]), Integer.parseInt(teamDataTokens[3]), Integer.parseInt(teamDataTokens[4]), Integer.parseInt(teamDataTokens[5]), Double.parseDouble(teamDataTokens[6]), Double.parseDouble(teamDataTokens[7]), Double.parseDouble(teamDataTokens[8]), Integer.parseInt(teamDataTokens[9]));
				if (groupD.contains(name)) insertData(name, count, "D", Double.parseDouble(teamDataTokens[1]), Double.parseDouble(teamDataTokens[2]), Integer.parseInt(teamDataTokens[3]), Integer.parseInt(teamDataTokens[4]), Integer.parseInt(teamDataTokens[5]), Double.parseDouble(teamDataTokens[6]), Double.parseDouble(teamDataTokens[7]), Double.parseDouble(teamDataTokens[8]), Integer.parseInt(teamDataTokens[9]));
				if (groupE.contains(name)) insertData(name, count, "E", Double.parseDouble(teamDataTokens[1]), Double.parseDouble(teamDataTokens[2]), Integer.parseInt(teamDataTokens[3]), Integer.parseInt(teamDataTokens[4]), Integer.parseInt(teamDataTokens[5]), Double.parseDouble(teamDataTokens[6]), Double.parseDouble(teamDataTokens[7]), Double.parseDouble(teamDataTokens[8]), Integer.parseInt(teamDataTokens[9]));
				if (groupF.contains(name)) insertData(name, count, "F", Double.parseDouble(teamDataTokens[1]), Double.parseDouble(teamDataTokens[2]), Integer.parseInt(teamDataTokens[3]), Integer.parseInt(teamDataTokens[4]), Integer.parseInt(teamDataTokens[5]), Double.parseDouble(teamDataTokens[6]), Double.parseDouble(teamDataTokens[7]), Double.parseDouble(teamDataTokens[8]), Integer.parseInt(teamDataTokens[9]));
				if (groupG.contains(name)) insertData(name, count, "G", Double.parseDouble(teamDataTokens[1]), Double.parseDouble(teamDataTokens[2]), Integer.parseInt(teamDataTokens[3]), Integer.parseInt(teamDataTokens[4]), Integer.parseInt(teamDataTokens[5]), Double.parseDouble(teamDataTokens[6]), Double.parseDouble(teamDataTokens[7]), Double.parseDouble(teamDataTokens[8]), Integer.parseInt(teamDataTokens[9]));
				if (groupH.contains(name)) insertData(name, count, "H", Double.parseDouble(teamDataTokens[1]), Double.parseDouble(teamDataTokens[2]), Integer.parseInt(teamDataTokens[3]), Integer.parseInt(teamDataTokens[4]), Integer.parseInt(teamDataTokens[5]), Double.parseDouble(teamDataTokens[6]), Double.parseDouble(teamDataTokens[7]), Double.parseDouble(teamDataTokens[8]), Integer.parseInt(teamDataTokens[9]));
				count++;
			}
		} catch (Exception e) {
			errorScreen(e.getMessage());
		}
	}
	
	// read the data file for the given team and return an array of all their stat values
	private String[] parseCSVData(String teamName) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.getClass().getResource("TeamData.csv").getPath()));
			String[] tokens = null;
			String line = null;
			while ((line = reader.readLine()) != null)
				if (line.contains(teamName)) {
					tokens = line.split(",");
					break;
				}
			reader.close();
			return tokens;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// insert a new team into the Teams table
	public void insertData(String teamName, int worldRank, String group, double aGS, double aGC, int qMP, int qGS, int qGC, double aC, double xgF, double xgA, int pRisk) {
		String query = "INSERT INTO WCTeams ([Name], [WorldRanking], [Group], [AvgGS], [AvgGC], [QMP], [QGS], [QGC], [avgCards], [xGf], [xGa], [predRisk]) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)){
			pstmt.setString(1, teamName);
			pstmt.setInt(2, worldRank);
			pstmt.setString(3, group);
			pstmt.setDouble(4, aGS);
			pstmt.setDouble(5, aGC);
			pstmt.setInt(6, qMP);
			pstmt.setInt(7, qGS);
			pstmt.setInt(8, qGC);
			pstmt.setDouble(9, aC);
			pstmt.setDouble(10, xgF);
			pstmt.setDouble(11, xgA);
			pstmt.setInt(12, pRisk);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			File f = new File("Teams.db");
			try {
				f.createNewFile();
				createTeamsTable();
				createMatchTable();
				insertData(teamName, worldRank, group, aGS, aGC, qMP, qGS, qGC, aC, xgF, xgA, pRisk);
			} catch (IOException e1) {
				errorScreen(e1.getMessage());
			}
		}
	}
	
	public static void main(String[] args) {
		new WCSimulatorGUI();
	}
	
	// remove all components from the container
	private void resetWindow() {
		con.removeAll();
		con.repaint();
		con.revalidate();
	}
	
	// create a new window for the application and register the custom fonts
	public WCSimulatorGUI() {
		window = new JFrame();
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
		window.setTitle("2022 World Cup Simulator");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setBackground(new Color(35, 2, 19));
		
		con = window.getContentPane();
		con.setBackground(new Color(35, 2, 19));
		
		try {
			titleFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("Qatar2022Arabic-Heavy.ttf")).deriveFont(65f);
			textFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("Qatar2022Arabic-Bold.ttf")).deriveFont(36f);
			buttonFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("Qatar2022Arabic-Bold.ttf")).deriveFont(22f);
			userTeamFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("Qatar2022Arabic-Heavy.ttf")).deriveFont(26f);
			statFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("Qatar2022Arabic-Bold.ttf")).deriveFont(16f);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(titleFont);
			ge.registerFont(textFont);
			ge.registerFont(buttonFont);
			ge.registerFont(statFont);
			ge.registerFont(userTeamFont);
		} catch (IOException | FontFormatException e) {
			errorScreen(e.getMessage());
		}
		
		mainMenu(0);
	}
	
	// initialise some global variables and create a main menu
	public void mainMenu(int playAgain) {
		clearDatabase();
		webScrape();
		
		groupCount = 0; matches = 0; KOmatches = -1; KOcount = 0; KOlimit = 7;
		userTeamProgressed = false; continueSim = false;
		stadiums = Arrays.asList("Al Thumama Stadium", "Al Janoub Stadium", "Al Bayt Stadium", "Ahmad bin Ali Stadium", "Education City Stadium", "Stadium 974",
				"Khalifa International Stadium", "Lusail Stadium");
		
		teams = retrieveData(15, false, false);
		List<String> groupA = new ArrayList<String>();
		List<String> groupB = new ArrayList<String>();
		List<String> groupC = new ArrayList<String>();
		List<String> groupD = new ArrayList<String>();
		List<String> groupE = new ArrayList<String>();
		List<String> groupF = new ArrayList<String>();
		List<String> groupG = new ArrayList<String>();
		List<String> groupH = new ArrayList<String>();
		List<String> teamNames = new ArrayList<String>();
		for (Team t : teams) {
			if (t.getGroup().equals("A")) groupA.add(t.getName());
			else if (t.getGroup().equals("B")) groupB.add(t.getName());
			else if (t.getGroup().equals("C")) groupC.add(t.getName());
			else if (t.getGroup().equals("D")) groupD.add(t.getName());
			else if (t.getGroup().equals("E")) groupE.add(t.getName());
			else if (t.getGroup().equals("F")) groupF.add(t.getName());
			else if (t.getGroup().equals("G")) groupG.add(t.getName());
			else if (t.getGroup().equals("H")) groupH.add(t.getName());
			teamNames.add(t.getName());
		}
		
		allGroupsString = Arrays.asList(groupA, groupB, groupC, groupD, groupE, groupF, groupG, groupH);
		
		if (playAgain == 0) {
			resetWindow();
			
			JLabel titleNameLabel = createLabel("2022 WORLD CUP SIMULATOR", titleFont, null);
			titleNameLabel.setBorder(new EmptyBorder(25, 0, 0, 0));
			con.add(titleNameLabel, BorderLayout.NORTH);
			
			JPanel titleChoicePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
			titleChoicePanel.setForeground(new Color(255, 255, 255));
			titleChoicePanel.setBackground(new Color(35, 2, 19));
			titleChoicePanel.setBorder(new EmptyBorder(0, 0, 10, 0));
			con.add(titleChoicePanel, BorderLayout.SOUTH);
			
			titleChoicePanel.add(createButton("CLASSIC MODE", "classicMode", choiceHandler, textFont));
			titleChoicePanel.add(createButton("SIMULATION MODE", "simMode", choiceHandler, textFont));
			titleChoicePanel.add(createButton("LOAD GAME", "loadGame", choiceHandler, textFont));
			
			JLabel gifLabel = playGIF("intro.gif");
			con.add(gifLabel, BorderLayout.CENTER);
			
			if (clip == null) playSound("mainMenuMusic.wav");
			window.setVisible(true);
		}
		else if (playAgain == 1) classicModeTable(allGroupsString, playAgain);
		else simulatingGroupStages(false);
	}
	
	// display all the teams in the groups for the user to choose from
	public void classicModeTable(List<List<String>> allGroups, int playAgain) {
		resetWindow();
		
		JLabel chooseTeamLabel = createLabel("Choose a Team", titleFont, null);
		chooseTeamLabel.setBorder(new EmptyBorder(25, 0, 25, 0));
		con.add(chooseTeamLabel, BorderLayout.NORTH);
		
		JPanel tablePanel = createPanel(new GridLayout(8, 5), 0);
		JPanel groupNamePanel = createPanel(new GridLayout(1, 8), 0);
		JLabel groupALabel = createLabel("GROUP A", textFont, border1);
		groupNamePanel.add(groupALabel);
		JLabel groupBLabel = createLabel("GROUP B", textFont, border1);
		groupNamePanel.add(groupBLabel);
		JLabel groupCLabel = createLabel("GROUP C", textFont, border1);
		groupNamePanel.add(groupCLabel);
		JLabel groupDLabel = createLabel("GROUP D", textFont, border1);
		groupNamePanel.add(groupDLabel);
		JLabel groupELabel = createLabel("GROUP E", textFont, border1);
		groupNamePanel.add(groupELabel);
		JLabel groupFLabel = createLabel("GROUP F", textFont, border1);
		groupNamePanel.add(groupFLabel);
		JLabel groupGLabel = createLabel("GROUP G", textFont, border1);
		groupNamePanel.add(groupGLabel);
		JLabel groupHLabel = createLabel("GROUP H", textFont, border2);
		groupNamePanel.add(groupHLabel);
		
		JPanel team1Panel = createPanel(new GridLayout(1, 8), 0);
		JPanel team2Panel = createPanel(new GridLayout(1, 8), 0);
		JPanel team3Panel = createPanel(new GridLayout(1, 8), 0);
		JPanel team4Panel = createPanel(new GridLayout(1, 8), 0);
		
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 4; j++) {
				JButton teamButton = createButton(allGroups.get(i).get(j), allGroups.get(i).get(j), choiceHandler, buttonFont);
				teamButton.setBorder(border1);
				teamButton.setForeground(Color.white);
				if (j == 0) team1Panel.add(teamButton);
				else if (j == 1) team2Panel.add(teamButton);
				else if (j == 2) team3Panel.add(teamButton);
				else team4Panel.add(teamButton);
			}
		
		tablePanel.add(groupNamePanel);
		tablePanel.add(team1Panel);
		tablePanel.add(team2Panel);
		tablePanel.add(team3Panel);
		tablePanel.add(team4Panel);
		
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 3);
		JButton backButton = createButton("Back", "mainMenu", choiceHandler, buttonFont);
		if (playAgain == 1) backButton.setText("Main Menu");
		buttonPanel.add(backButton);
		buttonPanel = addEmptySpaces(buttonPanel, 3);
		
		con.add(tablePanel, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// ask the user to confirm their choice of team
	public void confirmTeamSelection() {
		resetWindow();
		
		JLabel teamSelectedLabel = createLabel("You selected " + userTeamString, textFont, null);
		
		JPanel buttonPanel = createPanel(new GridLayout(1, 4), 1);
		JButton continueButton = createButton("Continue", "simGS", choiceHandler, buttonFont);
		buttonPanel.add(continueButton);
		JButton chooseAgainButton = createButton("Choose Again", "chooseAgain", choiceHandler, buttonFont);
		buttonPanel.add(chooseAgainButton);
		buttonPanel = addEmptySpaces(buttonPanel, 1);
		
		con.add(teamSelectedLabel, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// initialise the array of groups and reset some variables, if necessary
	public void simulatingGroupStages(boolean savedGame) {
		teams = retrieveData(15, false, false);
		List<Team> group1 = new ArrayList<Team>(); List<Team> group2 = new ArrayList<Team>(); List<Team> group3 = new ArrayList<Team>(); List<Team> group4 = new ArrayList<Team>();
		List<Team> group5 = new ArrayList<Team>(); List<Team> group6 = new ArrayList<Team>(); List<Team> group7 = new ArrayList<Team>(); List<Team> group8 = new ArrayList<Team>();
		List<Team> userGroup = new ArrayList<Team>();
		List<String> stringGroup = new ArrayList<String>();
		allGroups = new ArrayList<List<Team>>();
		int index = 0;
		if (!savedGame) {
			progressed = new ArrayList<Team>();
			playedAgainst = new ArrayList<String>();
			groupCount = 0; matches = 0; KOmatches = -1; KOcount = 0; KOlimit = 7;
		}
		
		for (Team t : teams) {
			if (t.getGroup().equals(userTeam.getGroup())) {
				userGroup.add(t);
				stringGroup.add(t.getName());
			} else {
				if (t.getGroup().equals("A")) group1.add(t);
				else if (t.getGroup().equals("B")) group2.add(t);
				else if (t.getGroup().equals("C")) group3.add(t);
				else if (t.getGroup().equals("D")) group4.add(t);
				else if (t.getGroup().equals("E")) group5.add(t);
				else if (t.getGroup().equals("F")) group6.add(t);
				else if (t.getGroup().equals("G")) group7.add(t);
				else if (t.getGroup().equals("H")) group8.add(t);
			}
		}
		
		if (group1.size() != 0) allGroups.add(group1);
		if (group2.size() != 0) allGroups.add(group2);
		if (group3.size() != 0) allGroups.add(group3);
		if (group4.size() != 0) allGroups.add(group4);
		if (group5.size() != 0) allGroups.add(group5);
		if (group6.size() != 0) allGroups.add(group6);
		if (group7.size() != 0) allGroups.add(group7);
		if (group8.size() != 0) allGroups.add(group8);
		
		if (userTeam.getGroup().equals("A")) index = 0;
		if (userTeam.getGroup().equals("B")) index = 1;
		if (userTeam.getGroup().equals("C")) index = 2;
		if (userTeam.getGroup().equals("D")) index = 3;
		if (userTeam.getGroup().equals("E")) index = 4;
		if (userTeam.getGroup().equals("F")) index = 5;
		if (userTeam.getGroup().equals("G")) index = 6;
		if (userTeam.getGroup().equals("H")) index = 7;
		
		allGroups.add(index, userGroup);
		
		simulateCMGroups(allGroups.get(groupCount));
	}
	
	// simulate all other groups apart from the user's team group
	public void simulateCMGroups(List<Team> group) {
		String hTeam, aTeam;
		int team1Goal = 0, team2Goal = 0, teamCounter = 0;
		int[] goals = new int[2];
		List<Team> temp = new ArrayList<Team>();
		
		if (group.get(0).getGPlayed() < 3) {
			if (!group.get(0).getGroup().equals(userTeam.getGroup())) {
				hTeam = generatingHomeTeam(group.get(0).getName(), group.get(1).getName());
				aTeam = generatingAwayTeam(group.get(0).getName(), group.get(1).getName(), hTeam);
				goals = bgGroupStageGame(hTeam, aTeam, team1Goal, team2Goal);
				team1Goal = goals[0];
				team2Goal = goals[1];
				winner(hTeam, aTeam, team1Goal, team2Goal, true, 0, 0, false, 0, 0, false);
				team1Goal = 0; team2Goal = 0; goals = new int[2];
				
				hTeam = generatingHomeTeam(group.get(2).getName(), group.get(3).getName());
				aTeam = generatingAwayTeam(group.get(2).getName(), group.get(3).getName(), hTeam);
				goals = bgGroupStageGame(hTeam, aTeam, team1Goal, team2Goal);
				team1Goal = goals[0];
				team2Goal = goals[1];
				winner(hTeam, aTeam, team1Goal, team2Goal, true, 0, 0, false, 0, 0, false);
				team1Goal = 0; team2Goal = 0; goals = new int[2];
				
				hTeam = generatingHomeTeam(group.get(0).getName(), group.get(2).getName());
				aTeam = generatingAwayTeam(group.get(0).getName(), group.get(2).getName(), hTeam);
				goals = bgGroupStageGame(hTeam, aTeam, team1Goal, team2Goal);
				team1Goal = goals[0];
				team2Goal = goals[1];
				winner(hTeam, aTeam, team1Goal, team2Goal, true, 0, 0, false, 0, 0, false);
				team1Goal = 0; team2Goal = 0; goals = new int[2];
				
				hTeam = generatingHomeTeam(group.get(1).getName(), group.get(3).getName());
				aTeam = generatingAwayTeam(group.get(1).getName(), group.get(3).getName(), hTeam);
				goals = bgGroupStageGame(hTeam, aTeam, team1Goal, team2Goal);
				team1Goal = goals[0];
				team2Goal = goals[1];
				winner(hTeam, aTeam, team1Goal, team2Goal, true, 0, 0, false, 0, 0, false);
				team1Goal = 0; team2Goal = 0; goals = new int[2];
				
				hTeam = generatingHomeTeam(group.get(0).getName(), group.get(3).getName());
				aTeam = generatingAwayTeam(group.get(0).getName(), group.get(3).getName(), hTeam);
				goals = bgGroupStageGame(hTeam, aTeam, team1Goal, team2Goal);
				team1Goal = goals[0];
				team2Goal = goals[1];
				winner(hTeam, aTeam, team1Goal, team2Goal, true, 0, 0, false, 0, 0, false);
				team1Goal = 0; team2Goal = 0; goals = new int[2];
				
				hTeam = generatingHomeTeam(group.get(1).getName(), group.get(2).getName());
				aTeam = generatingAwayTeam(group.get(1).getName(), group.get(2).getName(), hTeam);
				goals = bgGroupStageGame(hTeam, aTeam, team1Goal, team2Goal);
				team1Goal = goals[0];
				team2Goal = goals[1];
				winner(hTeam, aTeam, team1Goal, team2Goal, true, 0, 0, false, 0, 0, false);
				team1Goal = 0; team2Goal = 0; goals = new int[2];
				
				allGroups.set(groupCount, sortingSimulatedGroups(group));
				allGroups.get(groupCount).get(0).setTeamMorale(allGroups.get(groupCount).get(0).getTeamMorale() + 5);
				if (allGroups.get(groupCount).get(0).getWorldRank() > 20) allGroups.get(groupCount).get(0).setTeamMorale(allGroups.get(groupCount).get(0).getTeamMorale() + 5);
				allGroups.get(groupCount).get(1).setTeamMorale(allGroups.get(groupCount).get(1).getTeamMorale() + 5);
				if (allGroups.get(groupCount).get(1).getWorldRank() > 20) allGroups.get(groupCount).get(1).setTeamMorale(allGroups.get(groupCount).get(1).getTeamMorale() + 5);
				updateTeamMorale(allGroups.get(groupCount).get(0).getName(), allGroups.get(groupCount).get(0).getTeamMorale());
				updateTeamMorale(allGroups.get(groupCount).get(1).getName(), allGroups.get(groupCount).get(1).getTeamMorale());
				
				progressed.add(allGroups.get(groupCount).get(0));
				progressed.add(allGroups.get(groupCount).get(1));
			}
		}
		
		teams = retrieveData(15, false, false);
		for (Team t : teams)
			if (t.getGroup().equals(group.get(0).getGroup())) temp.add(t);
		
		Comparator<Team> comparebyGoalDiff = Comparator.comparing(Team::getGoalDiff);
		Comparator<Team> comparebyGoalsScored = Comparator.comparing(Team::getGoalsScored);
		Comparator<Team> compareTeam = Comparator.comparing(Team::getTotalPoints).thenComparing(comparebyGoalDiff).thenComparing(comparebyGoalsScored);
		temp = temp.stream().sorted(compareTeam).collect(Collectors.toList());
		Collections.reverse(temp);
		
		// display the group table screen
		resetWindow();
		
		JPanel groupTable = createPanel(new GridLayout(7, 1), 0);
		JLabel groupNameLabel = createLabel("Group " + temp.get(0).getGroup(), titleFont, null);
		groupNameLabel.setBorder(new EmptyBorder(25, 0, 25, 0));
		con.add(groupNameLabel, BorderLayout.NORTH);
		
		JPanel groupHeaderPanel = createPanel(new GridLayout(1, 5), 0);
		JLabel teamNameLabel = createLabel("Team Name", buttonFont, border1);
		groupHeaderPanel.add(teamNameLabel);
		JLabel gamesPlayedLabel = createLabel("Games Played", buttonFont, border1);
		groupHeaderPanel.add(gamesPlayedLabel);
		JLabel gamesWonLabel = createLabel("Games Won", buttonFont, border1);
		groupHeaderPanel.add(gamesWonLabel);
		JLabel gamesDrawnLabel = createLabel("Games Drawn", buttonFont, border1);
		groupHeaderPanel.add(gamesDrawnLabel);
		JLabel gamesLostLabel = createLabel("Games Lost", buttonFont, border1);
		groupHeaderPanel.add(gamesLostLabel);
		JLabel goalDiffLabel = createLabel("Goal Diff.", buttonFont, border1);
		groupHeaderPanel.add(goalDiffLabel);
		JLabel totalPointsLabel = createLabel("Total Points", buttonFont, border2);
		groupHeaderPanel.add(totalPointsLabel);
		groupTable.add(groupHeaderPanel);
		
		for (Team t : temp) {			
			JPanel teamStatsPanel = createPanel(new GridLayout(1, 7), 0);
			JLabel groupTeamNameLabel = createLabel(t.getName(), buttonFont, null);
			teamStatsPanel.add(groupTeamNameLabel);
			JLabel groupTeamGPLabel = createLabel(Integer.toString(t.getGPlayed()), buttonFont, null);
			teamStatsPanel.add(groupTeamGPLabel);
			JLabel groupTeamGWLabel = createLabel(Integer.toString(t.getGWon()), buttonFont, null);
			teamStatsPanel.add(groupTeamGWLabel);
			JLabel groupTeamGDLabel = createLabel(Integer.toString(t.getGDrawn()), buttonFont, null);
			teamStatsPanel.add(groupTeamGDLabel);
			JLabel groupTeamGLLabel = createLabel(Integer.toString(t.getGLost()), buttonFont, null);
			teamStatsPanel.add(groupTeamGLLabel);
			JLabel groupTeamGDiffLabel = createLabel(Integer.toString(t.getGoalDiff()), buttonFont, null);
			teamStatsPanel.add(groupTeamGDiffLabel);
			JLabel groupTeamTPLabel = createLabel(Integer.toString(t.getTotalPoints()), buttonFont, null);
			teamStatsPanel.add(groupTeamTPLabel);
			
			if (teamCounter < 3) {
				groupTeamNameLabel.setBorder(border1);
				groupTeamGPLabel.setBorder(border1);
				groupTeamGWLabel.setBorder(border1);
				groupTeamGDLabel.setBorder(border1);
				groupTeamGLLabel.setBorder(border1);
				groupTeamGDiffLabel.setBorder(border1);
				groupTeamTPLabel.setBorder(border2);
			} else {
				groupTeamNameLabel.setBorder(border3);
				groupTeamGPLabel.setBorder(border3);
				groupTeamGWLabel.setBorder(border3);
				groupTeamGDLabel.setBorder(border3);
				groupTeamGLLabel.setBorder(border3);
				groupTeamGDiffLabel.setBorder(border3);
			}
			
			if (t.getName().equals(userTeam.getName())) {
				groupTeamNameLabel.setFont(userTeamFont);
				groupTeamGPLabel.setFont(userTeamFont);
				groupTeamGWLabel.setFont(userTeamFont);
				groupTeamGDLabel.setFont(userTeamFont);
				groupTeamGLLabel.setFont(userTeamFont);
				groupTeamGDiffLabel.setFont(userTeamFont);
				groupTeamTPLabel.setFont(userTeamFont);
			}
			
			groupTable.add(teamStatsPanel);
			teamCounter++;
			con.add(groupTable, BorderLayout.CENTER);
			groupTable.setVisible(true);
		}
		
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 2);
		
		if (group.get(0).getGroup().equals(userTeam.getGroup())) {
			if (matches < 3) {
				JButton playGameButton = createButton("Play Game", "playGSGame", choiceHandler, buttonFont);
				buttonPanel.add(playGameButton);
			} else {
				boolean progressedCheck = false;
				allGroups.set(groupCount, sortingSimulatedGroups(group));
				for (Team t : progressed)
					if (t.getGroup().equals(userTeam.getGroup())) progressedCheck = true;
				
				if (!progressedCheck) {
					allGroups.get(groupCount).get(0).setTeamMorale(allGroups.get(groupCount).get(0).getTeamMorale() + 5);
					if (allGroups.get(groupCount).get(0).getWorldRank() > 20) allGroups.get(groupCount).get(0).setTeamMorale(allGroups.get(groupCount).get(0).getTeamMorale() + 5);
					allGroups.get(groupCount).get(1).setTeamMorale(allGroups.get(groupCount).get(1).getTeamMorale() + 5);
					if (allGroups.get(groupCount).get(1).getWorldRank() > 20) allGroups.get(groupCount).get(1).setTeamMorale(allGroups.get(groupCount).get(1).getTeamMorale() + 5);
					updateTeamMorale(allGroups.get(groupCount).get(0).getName(), allGroups.get(groupCount).get(0).getTeamMorale());
					updateTeamMorale(allGroups.get(groupCount).get(1).getName(), allGroups.get(groupCount).get(1).getTeamMorale());
					
					progressed.add(allGroups.get(groupCount).get(0));
					progressed.add(allGroups.get(groupCount).get(1));
				}
				
				if (groupCount < 7){
					JButton nextGroupButton = createButton("Next Group", "nextGroup", choiceHandler, buttonFont);
					buttonPanel.add(nextGroupButton);
				} else {
					JButton nextStageButton = createButton("Next Stage", "checkProgressed", choiceHandler, buttonFont);
					buttonPanel.add(nextStageButton);
				}
			}
			
			if (matches >= 3) {
				JButton gameResultsButton = createButton("Game Results", "gameResults", choiceHandler, buttonFont);
				buttonPanel.add(gameResultsButton);
			}

			if (matches < 3) {
				JButton saveGameButton = createButton("Save Game", "saveGame", choiceHandler, buttonFont);
				buttonPanel.add(saveGameButton);
			}
		} else {
			if (groupCount < 7) {
				JButton nextGroupButton = createButton("Next Group", "nextGroup", choiceHandler, buttonFont);
				buttonPanel.add(nextGroupButton);
			} else {
				JButton nextStageButton = createButton("Next Stage", "checkProgressed", choiceHandler, buttonFont);
				buttonPanel.add(nextStageButton);
			}
			
			JButton gameResultsButton = createButton("Game Results", "gameResults", choiceHandler, buttonFont);
			buttonPanel.add(gameResultsButton);
		}
		
		if (groupCount > 0) {
			JButton previousGroupButton = createButton("Previous Group", "previousGroup", choiceHandler, buttonFont);
			buttonPanel.add(previousGroupButton);
		}
		
		JButton exitGameButton = createButton("Exit Game", "exit", choiceHandler, buttonFont);
		buttonPanel.add(exitGameButton);
		
		buttonPanel = addEmptySpaces(buttonPanel, 2);
		con.add(buttonPanel, BorderLayout.SOUTH);
		con.setVisible(true);
	}
	
	// simulate a game with the user's team and display the result
	public void playGSGame() {
		Random rand = new Random();
		String groupLetter = allGroups.get(groupCount).get(0).getGroup();
		String stadiumName = stadiums.get(rand.nextInt(8));
		resetWindow();
		
		JPanel groupGameHeader = createPanel(new GridLayout(2, 1), 0);
		JLabel gameHeader = createLabel("Group " + groupLetter + " Round " + (matches + 1) + " Game Results", textFont, null);
		gameHeader.setBorder(new EmptyBorder(25, 0, 5, 0));
		groupGameHeader.add(gameHeader);
		JLabel stadiumHeader = createLabel(stadiumName, buttonFont, null);
		stadiumHeader.setBorder(new EmptyBorder(5, 0, 25, 0));
		groupGameHeader.add(stadiumHeader);
		con.add(groupGameHeader, BorderLayout.NORTH);
		
		int team1Score = 0, team2Score = 0;
		int indexOfUserTeam = allGroupsString.get(groupCount).indexOf(userTeam.getName());
		int randOpponent = rand.nextInt(4);
		while (playedAgainst.contains(allGroupsString.get(groupCount).get(randOpponent)) || randOpponent == indexOfUserTeam) randOpponent = rand.nextInt(4);
		String hTeam = generatingHomeTeam(allGroupsString.get(groupCount).get(indexOfUserTeam), allGroupsString.get(groupCount).get(randOpponent));
		String aTeam = generatingAwayTeam(allGroupsString.get(groupCount).get(indexOfUserTeam), allGroupsString.get(groupCount).get(randOpponent), hTeam);
		playedAgainst.add(allGroupsString.get(groupCount).get(randOpponent));
		int[] teamScores = groupStageGame(hTeam, aTeam, team1Score, team2Score);
		team1Score = teamScores[0];
		team2Score = teamScores[1];
		displayMatchReport(hTeam, aTeam, team1Score, team2Score, false, false, 0, 0);
		String Winner = winner(hTeam, aTeam, team1Score, team2Score, true, 0, 0, false, 0, 0, false);
		JLabel resultLabel = createLabel("", textFont, null);
		if (Winner.equals("")) {
			resultLabel.setText("It's a draw!");
			playSound("crowdWhistle.wav");
		} else {
			resultLabel.setText(Winner + " wins!");
			if (Winner.equals(userTeam.getName())) {
				resultLabel.setFont(userTeamFont.deriveFont(40f));
				playSound("crowdCheer.wav");
			} else playSound("crowdBoo.wav");
		}
		gsGameResultsPanel.add(resultLabel);
		
		int bgTeam1 = rand.nextInt(4);
		int bgTeam2 = rand.nextInt(4);
		while (bgTeam1 == indexOfUserTeam || bgTeam1 == randOpponent) bgTeam1 = rand.nextInt(4);
		while (bgTeam2 == indexOfUserTeam || bgTeam2 == randOpponent || bgTeam2 == bgTeam1) bgTeam2 = rand.nextInt(4);
		String hTeam2 = generatingHomeTeam(allGroupsString.get(groupCount).get(bgTeam1), allGroupsString.get(groupCount).get(bgTeam2));
		String aTeam2 = generatingAwayTeam(allGroupsString.get(groupCount).get(bgTeam1), allGroupsString.get(groupCount).get(bgTeam2), hTeam2);
		backgroundGroupStageGame(hTeam2, aTeam2);
		matches++;
		con.add(gsGameResultsPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = createPanel(new GridLayout(1, 5), 2);
		JButton checkGroupButton = createButton("Check Group", "checkGroup", choiceHandler, buttonFont);
		buttonPanel.add(checkGroupButton);
		buttonPanel = addEmptySpaces(buttonPanel, 2);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// create a match report for the given match
	private void displayMatchReport(String hTeam, String aTeam, int team1Score, int team2Score, boolean KOgame, boolean extraTime, int team1ETScore, int team2ETScore) {		
		Random rand = new Random();
		List<String> matchCommentary = Arrays.asList(" break away on the counter and finish off a good team move with a great goal!", " take a speculative shot from the edge of the box and it flies into the top corner!", " swing a corner in and they fire in a bullet header!",
				" get a penalty and they cooly convert!", " get a free kick and it soars into the top corner!", " takes a shot and the keeper spills it and it trickles in!", " takes a shot, it takes a deflection and wrongfoots the keeper!", " swing a cross in and they volley it off the post and in!", 
				" make a tackle, they go through 1-on-1 and they slot it home!", " cross it in from a corner, it bounces around the 6-yard box and they bundle it in!", " beat the offside trap and they confidently chip the keeper!", " get lucky as a horrible passback goes through the keeper's foot!", 
				" get lucky as a shanked clearance finds a way in the goal!", " get fortunate as their cross is headed home at the wrong end!", "'s cross is finshed off with a great header!", " get comical luck as the ball bounces around from a free kick and finds its way into the goal!", 
				" combine well at the edge of the box and they find a way through the defence to score!", " score a magical overhead kick!", " go a skillful, mazy run through the opposition and finish with aplomb!", " embarrass the opponents with great skill and finish emphatically!", " score from the halfway line with a looping shot!");
		int[] team1Mins = new int[team1Score];
		int[] team2Mins = new int[team2Score];
		int[] goalMins = new int[team1Score + team2Score];
		int[] team1ETMins = new int[team1ETScore];
		int[] team2ETMins = new int[team2ETScore];
		int[] goalETMins = new int[team1ETScore + team2ETScore];
		boolean team1UT = false;
		
		if (hTeam.equals(userTeam.getName())) team1UT = true;
		
		JPanel matchReport = createPanel(new GridLayout(team1Score + team2Score + team1ETScore + team2ETScore + 2, 1), 0);
		matchReport.setForeground(Color.WHITE);
		matchReport.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
		
		if (team1Score == 0 && team2Score == 0) {
			JLabel nilnil = createLabel("A close fought affair resulted in a boring draw...", buttonFont, null);
			matchReport.add(nilnil);
			matchReport = addEmptySpaces(matchReport, 1);
		} else {
			for (int i = 0; i < team1Mins.length; i++) {
				team1Mins[i] = rand.nextInt(90) + 1;
				goalMins[i] = team1Mins[i];
			}
			team1Mins = checkTeamGoalMins(team1Mins);

			for (int i = team1Mins.length; i < goalMins.length; i++) {
				int j = i - team1Mins.length;
				team2Mins[j] = rand.nextInt(90) + 1;
				if (j < team1Mins.length)
					if (team2Mins[j] == team1Mins[j]) team2Mins[j] += 1;
				goalMins[i] = team2Mins[j];
			}
			team2Mins = checkTeamGoalMins(team2Mins);
			
			Arrays.sort(goalMins);
			
			matchReport = addEmptySpaces(matchReport, 1);
			for (int i = 0; i < goalMins.length; i++) {
				JLabel goal = createLabel(goalMins[i] + "' : GOAL", buttonFont, null);
				
				for (int j = 0; j < team1Mins.length; j++) {
					if (team1Mins[j] == goalMins[i]) {
						goal.setText(goalMins[i] + "' : " + hTeam + matchCommentary.get(rand.nextInt(matchCommentary.size())));
						if (team1UT) goal.setFont(userTeamFont);
					}
				}
				
				for (int j = 0; j < team2Mins.length; j++) {
					if (team2Mins[j] == goalMins[i]) {
						goal.setText(goalMins[i] + "' : " + aTeam + matchCommentary.get(rand.nextInt(matchCommentary.size())));
						if (!team1UT) goal.setFont(userTeamFont);
					}
				}

				matchReport.add(goal);
			}
			
			if (extraTime) {
				for (int i = 0; i < team1ETMins.length; i++) {
					team1ETMins[i] = rand.nextInt(30) + 91;
					goalETMins[i] = team1ETMins[i];
				}
				team1ETMins = checkTeamGoalMins(team1ETMins);
				
				for (int i = team1ETMins.length; i < goalETMins.length; i++) {
					int j = i - team1ETMins.length;
					team2ETMins[j] = rand.nextInt(30) + 91;
					if (j < team1ETMins.length)
						if (team2ETMins[j] == team1ETMins[j]) team2ETMins[j] += 1;
					goalETMins[i] = team2ETMins[j];
				}
				team2ETMins = checkTeamGoalMins(team2ETMins);
				
				Arrays.sort(goalETMins);
				for (int i = 0; i < goalETMins.length; i++) {
					JLabel goal = createLabel(goalETMins[i] + "' : GOAL", buttonFont, null);
					
					for (int j = 0; j < team1ETMins.length; j++) {
						if (team1ETMins[j] == goalETMins[i]) {
							goal.setText(goalETMins[i] + "' : " + hTeam + matchCommentary.get(rand.nextInt(matchCommentary.size())));
							if (team1UT)goal.setFont(userTeamFont);
						}
					}
					
					for (int j = 0; j < team2ETMins.length; j++) {
						if (team2ETMins[j] == goalETMins[i]) {
							goal.setText(goalETMins[i] + "' : " + aTeam + matchCommentary.get(rand.nextInt(matchCommentary.size())));
							if (!team1UT)goal.setFont(userTeamFont);
						}
					}

					matchReport.add(goal);
				}
			}
			
			matchReport = addEmptySpaces(matchReport, 1);
		}
		if (KOgame) koGamesResults.add(matchReport);
		else gsGameResultsPanel.add(matchReport);
	}
	
	// check that that the minutes for each of the team's goals are different
	private int[] checkTeamGoalMins(int[] teamGoalMins) {
		if (teamGoalMins.length <= 1) return teamGoalMins;
		for (int i = 1; i < teamGoalMins.length; i++)
			if (teamGoalMins[i] == teamGoalMins[i-1]) teamGoalMins[i] += 1;
		return teamGoalMins;
	}

	// choose a team at random to be the home team
	private String generatingHomeTeam(String team1, String team2) {
		String homeTeam;
		Random rand = new Random();
		if (rand.nextInt(256) % 2 == 0) homeTeam = team1;
		else homeTeam = team2;
		return homeTeam;
	}
	
	// choose the other team to be the away team
	private String generatingAwayTeam(String team1, String team2, String homeTeam) {
		String awayTeam;
		if (homeTeam.equals(team1)) awayTeam = team2;
		else awayTeam = team1;
		return awayTeam;
	}
	
	// create the group stage game screen
	private int[] groupStageGame(String team1, String team2, int team1Goal, int team2Goal) {
		teams = retrieveData(15, false, false);
		team1Goal = 0; team2Goal = 0;
		int team1Rank = 0; int team2Rank = 0;
		
		for (Team t : teams) {
			if (t.getName().equals(team1)) team1Rank = t.getWorldRank();
			else if (t.getName().equals(team2)) team2Rank = t.getWorldRank();
		}
		
		int[] goals = goal(team1Rank, team2Rank, team1, team2, false);
		team1Goal = goals[0];
		team2Goal = goals[1];
		
		gsGameResultsPanel = createPanel(new GridLayout(3, 1), 0);
		JPanel game1 = createPanel(new GridLayout(1, 5), 0);
		JLabel g1HTeam = createLabel(team1, textFont, null);
		game1.add(g1HTeam);
		JLabel g1HTeamScore = createLabel(Integer.toString(team1Goal), textFont, null);
		game1.add(g1HTeamScore);
		JLabel vsLabel = createLabel(" - ", textFont, null);
		game1.add(vsLabel);
		JLabel g1ATeamScore = createLabel(Integer.toString(team2Goal), textFont, null);
		game1.add(g1ATeamScore);
		JLabel g1ATeam = createLabel(team2, textFont, null);
		game1.add(g1ATeam);
		
		if (team1.equals(userTeam.getName())) {
			g1HTeam.setFont(userTeamFont.deriveFont(45f));
			g1HTeamScore.setFont(userTeamFont.deriveFont(45f));
		}
		if (team2.equals(userTeam.getName())) {
			g1ATeam.setFont(userTeamFont.deriveFont(45f));
			g1ATeamScore.setFont(userTeamFont.deriveFont(45f));
		}
		
		gsGameResultsPanel.add(game1);
		
		return goals;
	}
	
	// generate the final score of a given match
	private int[] goal(int team1Rank, int team2Rank, String team1, String team2, boolean ET) {
		Random rand = new Random();
		DecimalFormat df = new DecimalFormat("0.0000");
		double[] meanScores = new double[2];
		int[] finalScore = new int[2];
		
		for (int i = 0; i < 2; i++) {
			double AGS = 0.0, OGC = 0.0, AC = 0.0, RC = 0.0, teamMorale = 0.0, teamPredRisk = 0.0;
			ArrayList<Double> teamChances;
			
			String team = team1, opp = team2;
			double teamRank = (double) team1Rank/100.0, oppRank = (double) team2Rank/100.0;
			if (i == 1) {
				team = team2;
				opp = team1;
				teamRank = (double) team2Rank/100.0;
				oppRank = (double) team1Rank/100.0;
			}
			for (Team t : teams)
				if (t.getName().equals(team)) {
					teamMorale = (double) t.getTeamMorale()/100.0;
					teamPredRisk = (double) t.getPredRisk()/100.0;
				}

			for (int x = 1; x < 4; x++) {
				// create Poisson distribution of given variable
				if (x == 1) teamChances = poissonModel(team, x, false, null);
				else teamChances = poissonModel(opp, x, false, null);
				
				for (int j = 1; j < 7; j++)
					teamChances.set(j, (teamChances.get(j) + teamChances.get(j - 1)));
				
				teamChances = monteCarloSim(teamChances);
				
				if (x == 1) AGS = teamChances.get(rand.nextInt(7));
				else if (x == 2) OGC = teamChances.get(rand.nextInt(7));
				else {
					AC = rand.nextInt(7);
					RC = poissonCalc(AC, 4) + poissonCalc(AC, 5) + poissonCalc(AC, 5);
					AC = AC / 4.0;
					if (AC < 1.0) AC = 0.0;
				}
			}			
			Double score = (AGS) + (OGC) - (AC * RC) + (oppRank - teamRank) + (teamMorale) - (teamPredRisk);
			Double meanScore = Double.parseDouble(df.format(Math.exp(score))) + 0.5;
			
			meanScores[i] = meanScore;
		}
		
		ArrayList<ArrayList<Double>> allScores = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> team1MeanScores = poissonModel(team1, 0, true, meanScores[0]),
				team2MeanScores = poissonModel(team2, 0, true, meanScores[1]),
				scores;

		// create Poisson distribution table
		for (int i = 0; i < 7; i++) {
			scores = new ArrayList<Double>();
			for (int j = 0; j < 7; j++) {
				Double prob = Double.parseDouble(df.format(team1MeanScores.get(i) * team2MeanScores.get(j)));
				if (prob > 0.0000) scores.add(prob);
			}
			if (scores.size() > 0) allScores.add(scores);
		}
		
		int team1index = 0, team2index = 0;
		for (int i = 0; i < allScores.size(); i++) {
			for (int j = 0; j < allScores.get(i).size(); j++) {
				if (allScores.get(i).get(j) > allScores.get(team1index).get(team2index)) {
					team1index = i;
					team2index = j;
				}
			}
		}
		
		finalScore[0] = team1index;
		finalScore[1] = team2index;
		
//		EXTRA GOALS FOR SPECIFIC TEAM FOR TESTING
//		if (team1Rank == 28) finalScore[0] = 0;
//		else if (team2Rank == 28) finalScore[1] = 0;
		
		for (Team t : teams) {
			if (t.getName().equals(team1)) {
				if (!ET) t.setGPlayed(t.getGPlayed() + 1);
				t.setGoalsScored(t.getGoalsScored() + finalScore[0]);
				t.setGoalsConceded(t.getGoalsConceded() + finalScore[1]);
				t.setGoalDiff(t.getGoalsScored() - t.getGoalsConceded());
				updateGoal(team1, t.getGPlayed(), t.getGoalsScored(), t.getGoalsConceded(), t.getGoalDiff(), t.getAvgGS(), t.getAvgGC());
			} else if (t.getName().equals(team2)) {
				if (!ET) t.setGPlayed(t.getGPlayed() + 1);
				t.setGoalsScored(t.getGoalsScored() + finalScore[1]);
				t.setGoalsConceded(t.getGoalsConceded() + finalScore[0]);
				t.setGoalDiff(t.getGoalsScored() - t.getGoalsConceded());
				updateGoal(team2, t.getGPlayed(), t.getGoalsScored(), t.getGoalsConceded(), t.getGoalDiff(), t.getAvgGS(), t.getAvgGC());
			}
		}
		
		return finalScore;
	}
	
	// generate a Poisson distribution for a given variable/mean and return the values in an array
	private ArrayList<Double> poissonModel(String team, int varNum, boolean score, Double mean) {
		ArrayList<Double> poisson = new ArrayList<Double>();
		Double var;
		
		if (score) {
			var = mean;
			for (int i = 0; i < 7; i++)
				poisson.add(poissonCalc(var, i));
		}
		else {
			var = retrieveVars(team, varNum);
			for (int i = 0; i < 7; i++)
				poisson.add(poissonCalc(var, i));
		}
		
		return poisson;
	}
	
	// perform the Poisson probability mass function
	private Double poissonCalc(Double var, int i) {
		Double[] factorials = new Double[] {1.0, 1.0, 2.0, 6.0, 24.0, 120.0, 720.0};
		return ((Math.pow(var, i)) * (Math.exp(-var))) / factorials[i];
	}
	
	// retrieve the value of the given variable for the given team, from the database
	private Double retrieveVars(String team, int varNum) {
		Double var1 = 0.0; Double var2 = 0.0, var3 = 0.0, var4 = 0.0;
		String query = "";
		
		if (varNum == 1) query = "SELECT WorldRanking, AvgGS, xGf, predRisk FROM WCTeams WHERE [Name] = '" + team + "'";
		else if (varNum == 2) query = "SELECT WorldRanking, AvgGC, xGa, predRisk FROM WCTeams WHERE [Name] = '" + team + "'";
		else query = "SELECT WorldRanking, avgCards, predRisk FROM WCTeams WHERE [Name] = '" + team + "'";
		
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				if (varNum == 3) var1 = rs.getDouble("avgCards");
				else if (varNum == 2) {
					var1 = rs.getDouble("AvgGC");
					var2 = rs.getDouble("xGa");
				}
				else { 
					var1 = rs.getDouble("AvgGS");
					var2 = rs.getDouble("xGf");
				}
				var3 = (double) rs.getInt("WorldRanking");
				var4 = (double) rs.getInt("predRisk")/100.0;
			}
			stmt.close();
		} catch (SQLException e) {
			errorScreen(e.getMessage());
		}
		
		// apply the modifications to the data
		if (varNum != 3) return Math.pow(var1, (var2/(var2 * var3 * var4)));
		return var1;
	}
	
	// perform a Monte Carlo simulation using the Poisson distribution values
	private ArrayList<Double> monteCarloSim(ArrayList<Double> poissonVals) {								   
	    int mPlayed = 0; int counter = 0;			   
	    double x;
	    int[] sumGS = new int[8];
	    ArrayList<Double> simVals = new ArrayList<Double>();
	    
	    for (int i = 0; i < 1000000; i++) {
			counter = 0;
	    	x = Math.random();
	    	mPlayed++;
	    	for (Double pV : poissonVals) {
	    		if (x <= pV) {
	    			sumGS[counter]++;
	    			break;
	    		}
	    		else counter++;
	    	}
	    }
	    
	    for (int i : sumGS)
	    	simVals.add((double) i/(double) mPlayed);
	    
	    simVals.remove(simVals.size()-1);
	    return simVals;
	}
	
	// update the all values relating to goals for a given team, in the database
	public void updateGoal(String teamName, int gamesPlayed, int goalsScored, int goalsConceded, int goalDiff, double avgGS, double avgGC) {
		String query = "UPDATE WCTeams SET GamesPlayed = ?, GoalsScored = ?, GoalsConceded = ?, GoalDifference = ?, AvgGS = ?, AvgGC = ? WHERE Name = ?";
		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, gamesPlayed);
			pstmt.setInt(2, goalsScored);
			pstmt.setInt(3, goalsConceded);
			pstmt.setInt(4, goalDiff);
			pstmt.setDouble(5, avgGS);
			pstmt.setDouble(6, avgGC);
			pstmt.setString(7, teamName);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			errorScreen(e.getMessage());
		}
	}
	
	// determine the winner of a given match, and update their values accordingly
	public String winner(String team1, String team2, int team1Score, int team2Score, boolean GS, int team1ETScore, int team2ETScore, boolean ET, int team1Pens, int team2Pens, boolean Pens) {
		teams = retrieveData(15, false, false);
		String winner = ""; String group = "KO";
		int team1Rank = 0, team2Rank = 0;
		int team1Result = team1Score + team1ETScore + team1Pens;
		int team2Result = team2Score + team2ETScore + team2Pens;
		
		for (Team t : teams) {
			if ((t.getName().equals(team1) || t.getName().equals(team2)) && GS) group = t.getGroup();
			if (t.getName().equals(team1)) team1Rank = t.getWorldRank();
			if (t.getName().equals(team2)) team2Rank = t.getWorldRank();
		}
				
		if (team1Result > team2Result) {
			winner = team1;
			int marginWin = team1Result - team2Result;
			int marginLoss = team2Result - team1Result;
			for (Team t : teams) {
				if (t.getName().equals(team1)) {
					t.setGWon(t.getGWon() + 1);
					t.setTeamMorale(t.getTeamMorale() + marginWin);
					if (team1Rank > team2Rank) {
						t.setTeamMorale(t.getTeamMorale() + 10);
						t.setPredRisk(t.getPredRisk() + 5);
					} else t.setPredRisk(t.getPredRisk() - 5);
					if (team1Rank > team2Rank + 15) {
						t.setTeamMorale(t.getTeamMorale() + 10);
						t.setPredRisk(t.getPredRisk() + 10);
					} else if (team2Rank > team1Rank + 15) t.setPredRisk(t.getPredRisk() - 10);
					if (t.getTeamMorale() > 100) t.setTeamMorale(100);
					if (t.getPredRisk() < 10) t.setPredRisk(10);
					if (GS) t.setTotalPoints(t.getTotalPoints() + 3);
					else if (ET) {
						t.setGDrawn(t.getGDrawn() + 1);
						t.setETWon(t.getETWon() + 1);
					}
					else if (Pens) {
						t.setGDrawn(t.getGDrawn() + 1);
						t.setETDrawn(t.getETDrawn() + 1);
						t.setPenWon(t.getPenWon() + 1);
					}
					updateData(team1, t.getGPlayed(), t.getGWon(), t.getGDrawn(), t.getGLost(), t.getTotalPoints(), t.getETWon(), t.getETDrawn(), t.getETLost(), t.getPenWon(), t.getPenLost(), t.getTeamMorale(), t.getPredRisk());
				} else if (t.getName().equals(team2)) {
					t.setGLost(t.getGLost() + 1);
					t.setTeamMorale(t.getTeamMorale() + marginLoss);
					if (team1Rank > team2Rank) {
						t.setTeamMorale(t.getTeamMorale() - 10);
						t.setPredRisk(t.getPredRisk() + 5);
					} else t.setPredRisk(t.getPredRisk() - 5);
					if (team1Rank > team2Rank + 15) {
						t.setTeamMorale(t.getTeamMorale() - 10);
						t.setPredRisk(t.getPredRisk() + 10);
					} else if (team2Rank > team1Rank + 15) t.setPredRisk(t.getPredRisk() - 10);
					if (t.getTeamMorale() < 10) t.setTeamMorale(10);
					if (t.getPredRisk() < 10) t.setPredRisk(10);
					if (ET) {
						t.setGDrawn(t.getGDrawn() + 1);
						t.setETLost(t.getETLost() + 1);
					}
					else if (Pens) {
						t.setGDrawn(t.getGDrawn() + 1);
						t.setETDrawn(t.getETDrawn() + 1);
						t.setPenLost(t.getPenLost() + 1);
					}
					updateData(team2, t.getGPlayed(), t.getGWon(), t.getGDrawn(), t.getGLost(), t.getTotalPoints(), t.getETWon(), t.getETDrawn(), t.getETLost(), t.getPenWon(), t.getPenLost(), t.getTeamMorale(), t.getPredRisk());
				}
			}
		} else if (team2Result > team1Result) {
			winner = team2;
			int marginWin = team2Result - team1Result;
			int marginLoss = team1Result - team2Result;
			for (Team t : teams) {
				if (t.getName().equals(team2)) {
					t.setGWon(t.getGWon() + 1);
					t.setTeamMorale(t.getTeamMorale() + marginWin);
					if (team2Rank > team1Rank) {
						t.setTeamMorale(t.getTeamMorale() + 10);
						t.setPredRisk(t.getPredRisk() + 5);
					} else t.setPredRisk(t.getPredRisk() - 5);
					if (team2Rank > team1Rank + 15) {
						t.setTeamMorale(t.getTeamMorale() + 10);
						t.setPredRisk(t.getPredRisk() + 10);
					} else if (team1Rank > team2Rank + 15) t.setPredRisk(t.getPredRisk() - 10);
					if (t.getTeamMorale() > 100) t.setTeamMorale(100);
					if (t.getPredRisk() < 10) t.setPredRisk(10);
					if (GS) t.setTotalPoints(t.getTotalPoints() + 3);
					else if (ET) {
						t.setGDrawn(t.getGDrawn() + 1);
						t.setETWon(t.getETWon() + 1);
					} else if (Pens) {
						t.setGDrawn(t.getGDrawn() + 1);
						t.setETDrawn(t.getETDrawn() + 1);
						t.setPenWon(t.getPenWon() + 1);
					}
					updateData(team2, t.getGPlayed(), t.getGWon(), t.getGDrawn(), t.getGLost(), t.getTotalPoints(), t.getETWon(), t.getETDrawn(), t.getETLost(), t.getPenWon(), t.getPenLost(), t.getTeamMorale(), t.getPredRisk());
				} else if (t.getName().equals(team1)) {
					t.setGLost(t.getGLost() + 1);
					t.setTeamMorale(t.getTeamMorale() + marginLoss);
					if (team2Rank > team1Rank) {
						t.setTeamMorale(t.getTeamMorale() - 10);
						t.setPredRisk(t.getPredRisk() + 5);
					} else t.setPredRisk(t.getPredRisk() - 5);
					if (team2Rank > team1Rank + 15) {
						t.setTeamMorale(t.getTeamMorale() - 10);
						t.setPredRisk(t.getPredRisk() + 10);
					} else if (team1Rank > team2Rank + 15) t.setPredRisk(t.getPredRisk() - 10);
					if (t.getTeamMorale() < 10) t.setTeamMorale(10);
					if (t.getPredRisk() < 10) t.setPredRisk(10);
					if (ET) {
						t.setGDrawn(t.getGDrawn() + 1);
						t.setETLost(t.getETLost() + 1);
					} else if (Pens) {
						t.setGDrawn(t.getGDrawn() + 1);
						t.setETDrawn(t.getETDrawn() + 1);
						t.setPenLost(t.getPenLost() + 1);
					}
					updateData(team1, t.getGPlayed(), t.getGWon(), t.getGDrawn(), t.getGLost(), t.getTotalPoints(), t.getETWon(), t.getETDrawn(), t.getETLost(), t.getPenWon(), t.getPenLost(), t.getTeamMorale(), t.getPredRisk());
				}
			}
		} else {
			winner = "";
			for (Team t : teams) {
				if (t.getName().equals(team1)) {
					t.setGDrawn(t.getGDrawn() + 1);
					if (team1Rank > team2Rank) t.setTeamMorale(t.getTeamMorale() + 5);
					else t.setTeamMorale(t.getTeamMorale() - 5);
					t.setPredRisk(t.getPredRisk() + 2);
					if (team1Rank > team2Rank + 15) {
						t.setTeamMorale(t.getTeamMorale() + 5);
						t.setPredRisk(t.getPredRisk() + 5);
					} else if (team2Rank > team1Rank + 15) {
						t.setTeamMorale(t.getTeamMorale() - 5);
						t.setPredRisk(t.getPredRisk() + 5);
					} else t.setPredRisk(t.getPredRisk() - 2);
					if (t.getTeamMorale() > 100) t.setTeamMorale(100);
					else if (t.getTeamMorale() < 10) t.setTeamMorale(10);
					if (t.getPredRisk() < 10) t.setPredRisk(10);
					if (GS) t.setTotalPoints(t.getTotalPoints() + 1);
					updateData(team1, t.getGPlayed(), t.getGWon(), t.getGDrawn(), t.getGLost(), t.getTotalPoints(), t.getETWon(), t.getETDrawn(), t.getETLost(), t.getPenWon(), t.getPenLost(), t.getTeamMorale(), t.getPredRisk());
				} else if (t.getName().equals(team2)) {
					t.setGDrawn(t.getGDrawn() + 1);
					if (team2Rank > team1Rank) t.setTeamMorale(t.getTeamMorale() + 5);
					else t.setTeamMorale(t.getTeamMorale() - 5);
					t.setPredRisk(t.getPredRisk() + 2);
					if (team2Rank > team1Rank + 15) {
						t.setTeamMorale(t.getTeamMorale() + 5);
						t.setPredRisk(t.getPredRisk() + 5);
					} else if (team1Rank > team2Rank + 15) {
						t.setTeamMorale(t.getTeamMorale() - 5);
						t.setPredRisk(t.getPredRisk() + 5);
					} else t.setPredRisk(t.getPredRisk() - 2);
					if (t.getTeamMorale() > 100) t.setTeamMorale(100);
					else if (t.getTeamMorale() < 10) t.setTeamMorale(10);
					if (t.getPredRisk() < 10) t.setPredRisk(10);
					if (GS) t.setTotalPoints(t.getTotalPoints() + 1);
					updateData(team2, t.getGPlayed(), t.getGWon(), t.getGDrawn(), t.getGLost(), t.getTotalPoints(), t.getETWon(), t.getETDrawn(), t.getETLost(), t.getPenWon(), t.getPenLost(), t.getTeamMorale(), t.getPredRisk());
				}
			}
		}
		
		updateMatches(team1, team1Score, team2Score, team2, group, team1ETScore, team2ETScore, team1Pens, team2Pens);
		
		return winner;
	}
	
	// update the data of a given team, in the database
	public void updateData(String teamName, int gamesPlayed, int gamesWon, int gamesDrawn, int gamesLost, int totalPoints, int etWon, int etDrawn, int etLost, int penWon, int penLost, int teamMorale, int predRisk) {
		String query = "UPDATE WCTeams SET GamesPlayed = ?, GamesWon = ?, GamesDrawn = ?, GamesLost = ?, TotalPoints = ?, ETWon = ?,  ETDrawn = ?, ETLost = ?, PenWon = ?, PenLost = ?, TeamMorale = ?, predRisk = ? WHERE Name = ?";
		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, gamesPlayed);
			pstmt.setInt(2, gamesWon);
			pstmt.setInt(3, gamesDrawn);
			pstmt.setInt(4, gamesLost);
			pstmt.setInt(5, totalPoints);
			pstmt.setInt(6, etWon);
			pstmt.setInt(7, etDrawn);
			pstmt.setInt(8, etLost);
			pstmt.setInt(9, penWon);
			pstmt.setInt(10, penLost);
			pstmt.setInt(11, teamMorale);
			pstmt.setInt(12, predRisk);
			pstmt.setString(13, teamName);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			errorScreen(e.getMessage());
		}
	}
	
	// add the completed match into the database
	public void updateMatches(String team1, int team1Score, int team2Score, String team2, String group, int team1ETScore, int team2ETScore, int team1Pens, int team2Pens) {
		String query = "INSERT INTO WCMatches ([hTeam], [hTeamGoals], [aTeamGoals], [aTeam], [Group], [hTeamETGoals], [aTeamETGoals], [hTeamPens], [aTeamPens]) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, team1);
			pstmt.setInt(2, team1Score);
			pstmt.setInt(3, team2Score);
			pstmt.setString(4, team2);
			pstmt.setString(5, group);
			pstmt.setInt(6, team1ETScore);
			pstmt.setInt(7, team2ETScore);
			pstmt.setInt(8, team1Pens);
			pstmt.setInt(9, team2Pens);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			createMatchTable();
		}
	}
	
	// simulate the other group stage game
	public void backgroundGroupStageGame(String hTeam, String aTeam) {
		int team1Goal = 0; int team2Goal = 0;
		int[] goals = bgGroupStageGame(hTeam, aTeam, team1Goal, team2Goal);
		team1Goal = goals[0];
		team2Goal = goals[1];
		
		winner(hTeam, aTeam, team1Goal, team2Goal, true, 0, 0, false, 0, 0, false);
	}
	
	// get the rankings of both teams and return the final score using the goal method
	public int[] bgGroupStageGame(String team1, String team2, int team1Goal, int team2Goal) {
		team1Goal = 0; team2Goal = 0;
		int team1Rank = 0; int team2Rank = 0;
		teams = retrieveData(15, false, false);
		
		for (Team t : teams) {
			if (t.getName().equals(team1)) team1Rank = t.getWorldRank();
			else if (t.getName().equals(team2)) team2Rank = t.getWorldRank();
		}
		
		return goal(team1Rank, team2Rank, team1, team2, false);
	}
	
	// sort the simulated groups by total points, goal difference and goals scored
	public List<Team> sortingSimulatedGroups(List<Team> group) {
		teams = retrieveData(15, false, false);
		List<Team> temp = new ArrayList<Team>();
		String groupLetter = group.get(0).getGroup();
		
		for (Team t : teams)
			if (t.getGroup().equals(groupLetter)) temp.add(t);
		
		Comparator<Team> comparebyGoalDiff = Comparator.comparing(Team::getGoalDiff);
		Comparator<Team> comparebyGoalsScored = Comparator.comparing(Team::getGoalsScored);
		Comparator<Team> compareTeam = Comparator.comparing(Team::getTotalPoints).thenComparing(comparebyGoalDiff).thenComparing(comparebyGoalsScored);
		temp = temp.stream().sorted(compareTeam).collect(Collectors.toList());
		Collections.reverse(temp);
		
		return temp;
	}
	
	// check if the user's team has progressed to the next stage
	public void checkUserTeamProgressed() {
		resetWindow();
		
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 2);
		
		userTeamProgressed = false;
		
		if ((KOmatches + 1) == 0) {
			for (int i = 0; i < 16; i++)
				if (progressed.get(i).getName().equals(userTeam.getName())) userTeamProgressed = true;
		} else if ((KOmatches + 1) == 1) {
			for (int i = 16; i < 24; i++)
				if (progressed.get(i).getName().equals(userTeam.getName())) userTeamProgressed = true;
		} else if ((KOmatches + 1) == 2) {
			for (int i = 24; i < 28; i++)
				if (progressed.get(i).getName().equals(userTeam.getName())) userTeamProgressed = true;
		} else if ((KOmatches + 1) == 3) {
			for (int i = 28; i < 30; i++)
				if (progressed.get(i).getName().equals(userTeam.getName())) userTeamProgressed = true;
		}
		
		JLabel nextLabel = createLabel("", textFont, null);
		if (userTeamProgressed) {
			nextLabel.setText("Congratulations! Your team progressed to the knockout stages!");
			if ((KOmatches + 1) == 1) nextLabel.setText("Congratulations! Your team progressed to the quarter finals!");
			else if ((KOmatches + 1) == 2) nextLabel.setText("Congratulations! Your team progressed to the semi finals!");
			else if ((KOmatches + 1) == 3) nextLabel.setText("Congratulations! Your team progressed to the World Cup final!");
			nextLabel.setBorder(new EmptyBorder(25, 0, 0, 0));
		} else {
			if (!continueSim) {
				JLabel notProgressedLabel = createLabel("Unlucky! Your team did not progress to the knockout stages.", textFont, null);
				if ((KOmatches + 1) == 1) notProgressedLabel.setText("Unlucky! Your team did not progress to the quarter finals.");
				else if ((KOmatches + 1) == 2) notProgressedLabel.setText("Unlucky! Your team did not progress to the semi finals.");
				else if ((KOmatches + 1) == 3) notProgressedLabel.setText("Unlucky! Your team did not progress to the World Cup final.");
				notProgressedLabel.setBorder(new EmptyBorder(25, 0, 0, 0));
				statsPage(notProgressedLabel);
			} else {
				nextLabel.setText("Moving onto the quarter finals...");
				if (KOmatches == 1) nextLabel.setText("Moving onto the semi finals...");
				else if (KOmatches == 2) nextLabel.setText("Moving onto the World Cup final...");
			}
		}
		
		if (userTeamProgressed || (!userTeamProgressed && continueSim)) {
			JButton continueSimButton = createButton("Continue", "nextStage", choiceHandler, buttonFont);
			buttonPanel.add(continueSimButton);
			buttonPanel = addEmptySpaces(buttonPanel, 2);
			con.add(nextLabel, BorderLayout.CENTER);
			con.add(buttonPanel, BorderLayout.SOUTH);
		}
	}
	
	// determine which stage the user is at and initialise the stage match-ups
	public void simulatingKnockoutStages(boolean savedGame) {		
		if (KOmatches == 0) {
			if (!savedGame) KOcount = 0;
			List<Team> R16_1 = Arrays.asList(progressed.get(0), progressed.get(3));
			List<Team> R16_2 = Arrays.asList(progressed.get(4), progressed.get(7));
			List<Team> R16_3 = Arrays.asList(progressed.get(8), progressed.get(11));
			List<Team> R16_4 = Arrays.asList(progressed.get(12), progressed.get(15));
			List<Team> R16_5 = Arrays.asList(progressed.get(2), progressed.get(1));
			List<Team> R16_6 = Arrays.asList(progressed.get(6), progressed.get(5));
			List<Team> R16_7 = Arrays.asList(progressed.get(10), progressed.get(9));
			List<Team> R16_8 = Arrays.asList(progressed.get(14), progressed.get(13));
			allR16s = Arrays.asList(R16_1, R16_2, R16_3, R16_4, R16_5, R16_6, R16_7, R16_8);
			backgroundKnockoutGames(allR16s.get(KOcount));
		} else if (KOmatches == 1) {
			if (!savedGame) KOcount = 0;
			List<Team> QF_1 = Arrays.asList(progressed.get(16), progressed.get(17));
			List<Team> QF_2 = Arrays.asList(progressed.get(18), progressed.get(19));
			List<Team> QF_3 = Arrays.asList(progressed.get(20), progressed.get(21));
			List<Team> QF_4 = Arrays.asList(progressed.get(22), progressed.get(23));
			allQFs = Arrays.asList(QF_1, QF_2, QF_3, QF_4);
			backgroundKnockoutGames(allQFs.get(KOcount));
		} else if (KOmatches == 2) {
			if (!savedGame) KOcount = 0;
			List<Team> SF_1 = Arrays.asList(progressed.get(24), progressed.get(25));
			List<Team> SF_2 = Arrays.asList(progressed.get(26), progressed.get(27));
			allSFs = Arrays.asList(SF_1, SF_2);
			backgroundKnockoutGames(allSFs.get(KOcount));
		} else if (KOmatches == 3) {
			WCfinal = Arrays.asList(progressed.get(28), progressed.get(29));
			backgroundKnockoutGames(WCfinal);
		}
	}
	
	// simulate all the given knock-out stage matches apart from the user's team, and allow the user to cycle through the match results
	public void backgroundKnockoutGames(List<Team> KOprogressed) {
		String hTeam, aTeam;
		int team1Goal = 0; int team2Goal = 0;
		teams = retrieveData(15, false, false);
		
		for (Team t : teams) {
			if (t.getName().equals(KOprogressed.get(0).getName())) KOprogressed.set(0, t);
			else if (t.getName().equals(KOprogressed.get(1).getName())) KOprogressed.set(1, t);
		}
		
		resetWindow();

		if ((KOprogressed.get(0).getGPlayed() - 3) == KOmatches) {
			if (!KOprogressed.get(0).getName().equals(userTeam.getName()) && !KOprogressed.get(1).getName().equals(userTeam.getName())) {
				hTeam = generatingHomeTeam(KOprogressed.get(0).getName(), KOprogressed.get(1).getName());
				aTeam = generatingAwayTeam(KOprogressed.get(0).getName(), KOprogressed.get(1).getName(), hTeam);
				String Winner = bgKnockoutGame(hTeam, aTeam, team1Goal, team2Goal, false);
				if (Winner.equals(KOprogressed.get(0).getName())) progressed.add(KOprogressed.get(0));
				else if (Winner.equals(KOprogressed.get(1).getName())) progressed.add(KOprogressed.get(1));
				team1Goal = 0; team2Goal = 0;
			}
		} else {			
			List<Match> koMatch = retrieveMatches("KO", "", "");
			JLabel gameLabel = createLabel("", textFont, null);
			gameLabel.setBorder(new EmptyBorder(25, 0, 0 ,0));
			if (KOmatches == 0) gameLabel.setText("ROUND OF 16 GAME " + (KOcount + 1));
			if (KOmatches == 1) gameLabel.setText("QUARTER FINAL GAME " + (KOcount + 1));
			if (KOmatches == 2) gameLabel.setText("SEMI FINAL GAME " + (KOcount + 1));
			con.add(gameLabel, BorderLayout.NORTH);
			
			koGamesResults = createPanel(new GridLayout(5, 1), 0);
			JPanel KOgame = createPanel(new GridLayout(1, 5), 0);
			JLabel KOhTeam = createLabel(koMatch.get(0).gethTeam(), textFont, null);
			KOgame.add(KOhTeam);
			JLabel KOhTeamScore = createLabel(Integer.toString(koMatch.get(0).gethTeamGoals()), textFont, null);
			KOgame.add(KOhTeamScore);
			JLabel vsLabel = createLabel(" - ", textFont, null);
			KOgame.add(vsLabel);
			JLabel KOaTeamScore = createLabel(Integer.toString(koMatch.get(0).getaTeamGoals()), textFont, null);
			KOgame.add(KOaTeamScore);
			JLabel KOaTeam = createLabel(koMatch.get(0).getaTeam(), textFont, null);
			KOgame.add(KOaTeam);
			koGamesResults.add(KOgame);
			
			if (koMatch.get(0).gethTeam().equals(userTeam.getName())) {
				KOhTeam.setFont(userTeamFont.deriveFont(45f));
				KOhTeamScore.setFont(userTeamFont.deriveFont(45f));
			} else if (koMatch.get(0).getaTeam().equals(userTeam.getName())) {
				KOaTeam.setFont(userTeamFont.deriveFont(45f));
				KOaTeamScore.setFont(userTeamFont.deriveFont(45f));
			}
			
			JLabel winnerLabel = createLabel("", textFont, null);
			if (koMatch.get(0).gethTeamGoals() == koMatch.get(0).getaTeamGoals()) {
				JLabel etResultLabel = createLabel("FINAL RESULT (AFTER EXTRA TIME):", textFont, null);
				koGamesResults.add(etResultLabel);
				JPanel etKOgame = createPanel(new GridLayout(1, 5), 0);
				JLabel etKOhTeam = createLabel(koMatch.get(0).gethTeam(), textFont, null);
				etKOgame.add(etKOhTeam);
				JLabel etKOhTeamScore = createLabel(Integer.toString(koMatch.get(0).gethTeamGoals() + koMatch.get(0).gethTeamETGoals()), textFont, null);
				etKOgame.add(etKOhTeamScore);
				JLabel etVsLabel = createLabel(" - ", textFont, null);
				etKOgame.add(etVsLabel);
				JLabel etKOaTeamScore = createLabel(Integer.toString(koMatch.get(0).getaTeamGoals() + koMatch.get(0).getaTeamETGoals()), textFont, null);
				etKOgame.add(etKOaTeamScore);
				JLabel etKOaTeam = createLabel(koMatch.get(0).getaTeam(), textFont, null);
				etKOgame.add(etKOaTeam);
				koGamesResults.add(etKOgame);
				
				if (koMatch.get(0).gethTeam().equals(userTeam.getName())) {
					etKOhTeam.setFont(userTeamFont.deriveFont(45f));
					etKOhTeamScore.setFont(userTeamFont.deriveFont(45f));
				} else if (koMatch.get(0).getaTeam().equals(userTeam.getName())) {
					etKOaTeam.setFont(userTeamFont.deriveFont(45f));
					etKOaTeamScore.setFont(userTeamFont.deriveFont(45f));
				}
				
				if (koMatch.get(0).gethTeamETGoals() == koMatch.get(0).getaTeamETGoals()) {
					if (koMatch.get(0).gethTeamPens() > koMatch.get(0).getaTeamPens()) winnerLabel.setText(koMatch.get(0).gethTeam() + " wins " + koMatch.get(0).gethTeamPens() + "-" + koMatch.get(0).getaTeamPens() + " on penalties!");
					else winnerLabel.setText(koMatch.get(0).getaTeam() + " wins " + koMatch.get(0).getaTeamPens() + "-" + koMatch.get(0).gethTeamPens() + " on penalties!");
				} else {
					if (koMatch.get(0).gethTeamETGoals() > koMatch.get(0).getaTeamETGoals()) winnerLabel.setText(koMatch.get(0).gethTeam() + " wins in extra time!");
					else winnerLabel.setText(koMatch.get(0).getaTeam() + " wins in extra time!");
				}
			} else {
				if (koMatch.get(0).gethTeamGoals() > koMatch.get(0).getaTeamGoals()) winnerLabel.setText(koMatch.get(0).gethTeam() + " wins in regular time!");
				else winnerLabel.setText(koMatch.get(0).getaTeam() + " wins in regular time!");
			}
			
			if (winnerLabel.getText().contains(userTeam.getName())) winnerLabel.setFont(userTeamFont.deriveFont(45f));
			
			koGamesResults.add(winnerLabel);
			con.add(koGamesResults, BorderLayout.CENTER);
		}
		
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 0);
		
		if (KOprogressed.get(0).getName().equals(userTeam.getName()) || KOprogressed.get(1).getName().equals(userTeam.getName())) {
			if ((KOprogressed.get(0).getGPlayed() - 3) == KOmatches) {
				JLabel gameLabel = createLabel("", textFont, null);
				gameLabel.setBorder(new EmptyBorder(25, 0, 0 ,0));
				if (KOmatches == 0) gameLabel.setText("ROUND OF 16 GAME " + (KOcount + 1));
				if (KOmatches == 1) gameLabel.setText("QUARTER FINAL GAME " + (KOcount + 1));
				if (KOmatches == 2) gameLabel.setText("SEMI FINAL GAME " + (KOcount + 1));
				if (KOmatches == 3) gameLabel.setText("WORLD CUP FINAL");
				con.add(gameLabel, BorderLayout.NORTH);
				
				koGamesResults = createPanel(new GridLayout(4, 1), 0);
				JPanel KOgame = createPanel(new GridLayout(1, 5), 0);
				JLabel koHTeam = createLabel(KOprogressed.get(0).getName(), textFont, null);
				KOgame.add(koHTeam);
				JLabel vsLabel = createLabel(" - ", textFont, null);
				KOgame.add(vsLabel);
				JLabel koATeam = createLabel(KOprogressed.get(1).getName(), textFont, null);
				KOgame.add(koATeam);
				
				if (KOprogressed.get(0).getName().equals(userTeam.getName())) koHTeam.setFont(userTeamFont.deriveFont(45f));
				else if (KOprogressed.get(1).getName().equals(userTeam.getName())) koATeam.setFont(userTeamFont.deriveFont(45f));
				
				koGamesResults.add(KOgame);
				con.add(koGamesResults, BorderLayout.CENTER);
			}
			
			buttonPanel = addEmptySpaces(buttonPanel, 2);
			
			if (KOmatches < 4 && ((KOprogressed.get(0).getGPlayed() - 3) == KOmatches)) {
				JButton playGameButton = createButton("Play Game", "playKOGame", choiceHandler, buttonFont);
				buttonPanel.add(playGameButton);
			} else {
				if (KOcount != KOlimit) {
					userPlayedKOgame = false;
					JButton nextGameButton = createButton("Next Game", "nextGame", choiceHandler, buttonFont);
					buttonPanel.add(nextGameButton);
				} else {
					if (KOmatches == 3) {
						JButton WCwinnerButton = createButton("Continue", "nextStage", choiceHandler, buttonFont);
						buttonPanel.add(WCwinnerButton);
					} else {
						JButton nextStageButton = createButton("Next Stage", "checkProgressed", choiceHandler, buttonFont);
						buttonPanel.add(nextStageButton);
					}
				}
			}
			
			if (KOcount > 0 && KOmatches != 3) {
				JButton previousGameButton = createButton("Previous Game", "previousGame", choiceHandler, buttonFont);
				buttonPanel.add(previousGameButton);
			}
			
			if (KOmatches != 4) {
				JButton saveGameButton = createButton("Save Game", "saveGame", choiceHandler, buttonFont);
				buttonPanel.add(saveGameButton);
			}
		} else {
			buttonPanel = addEmptySpaces(buttonPanel, 2);
			
			if (KOcount < KOlimit) {
				userPlayedKOgame = false;
				JButton nextGameButton = createButton("Next Game", "nextGame", choiceHandler, buttonFont);
				buttonPanel.add(nextGameButton);
			} else {
				if (KOmatches == 3) {
					JButton finishButton = createButton("Finish", "rateSim", choiceHandler, buttonFont);
					buttonPanel.add(finishButton);
				} else {
					JButton nextStageButton = createButton("Next Stage", "checkProgressed", choiceHandler, buttonFont);
					buttonPanel.add(nextStageButton);
				}
			}
			if (KOcount > 0 && KOmatches != 3) {
				JButton previousGameButton = createButton("Previous Game", "previousGame", choiceHandler, buttonFont);
				buttonPanel.add(previousGameButton);
			}
		}
		JButton exitGameButton = createButton("Exit Game", "exit", choiceHandler, buttonFont);
		buttonPanel.add(exitGameButton);
		
		buttonPanel = addEmptySpaces(buttonPanel, 2);
		con.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setVisible(true);
	}
	
	// simulate the user's team knock-out stage match-up and display the results
	public void playKOGame(String team1, String team2) {
		Random rand = new Random();
		int team1Goal = 0; int team2Goal = 0;
		int team1Rank = 0; int team2Rank = 0;
		int team1Pens = 0; int team2Pens = 0;
		String Winner = "";
		teams = retrieveData(15, false, false);
		for (Team t : teams) {
			if (t.getName().equals(team1)) team1Rank = t.getWorldRank();
			else if (t.getName().equals(team2)) team2Rank = t.getWorldRank();
		}
		
		int[] goals = goal(team1Rank, team2Rank, team1, team2, false);
		team1Goal = goals[0];
		team2Goal = goals[1];
		
		resetWindow();
		
		String stadiumName = stadiums.get(rand.nextInt(8));
		JPanel koGameHeader = createPanel(new GridLayout(2, 1), 0);
		JLabel gameLabel = createLabel("", textFont, null);
		gameLabel.setBorder(new EmptyBorder(25, 0, 5 ,0));
		if (KOmatches == 0) gameLabel.setText("ROUND OF 16 GAME " + (KOcount + 1));
		if (KOmatches == 1) gameLabel.setText("QUARTER FINAL GAME " + (KOcount + 1));
		if (KOmatches == 2) gameLabel.setText("SEMI FINAL GAME " + (KOcount + 1));
		if (KOmatches == 3) gameLabel.setText("WORLD CUP FINAL");
		koGameHeader.add(gameLabel);
		JLabel stadiumHeader = createLabel(stadiumName, buttonFont, null);
		stadiumHeader.setBorder(new EmptyBorder(5, 0, 25, 0));
		koGameHeader.add(stadiumHeader);
		con.add(koGameHeader, BorderLayout.NORTH);
		
		koGamesResults = createPanel(new GridLayout(3, 1), 0);
		JPanel KOgame = createPanel(new GridLayout(1, 5), 0);
		JLabel koHTeam = createLabel(team1, textFont, null);
		KOgame.add(koHTeam);
		JLabel koHTeamScore = createLabel(Integer.toString(team1Goal), textFont, null);
		KOgame.add(koHTeamScore);
		JLabel vsLabel = createLabel(" - ", textFont, null);
		KOgame.add(vsLabel);
		JLabel koATeamScore = createLabel(Integer.toString(team2Goal), textFont, null);
		KOgame.add(koATeamScore);
		JLabel koATeam = createLabel(team2, textFont, null);
		KOgame.add(koATeam);
		
		if (team1.equals(userTeam.getName())) {
			koHTeam.setFont(userTeamFont.deriveFont(45f));
			koHTeamScore.setFont(userTeamFont.deriveFont(45f));
		} else if (team2.equals(userTeam.getName())) {
			koATeam.setFont(userTeamFont.deriveFont(45f));
			koATeamScore.setFont(userTeamFont.deriveFont(45f));
		}
		
		koGamesResults.add(KOgame);
		
		if (team1Goal == team2Goal) {			
			int[] etGoals = goal(team1Rank, team2Rank, team1, team2, true);
			team1Goal += etGoals[0];
			team2Goal += etGoals[1];
			
			koHTeamScore.setText("(" + (team1Goal - etGoals[0]) + ") " + team1Goal);
			koATeamScore.setText(team2Goal + " (" + (team2Goal - etGoals[1]) + ")");
						
			displayMatchReport(team1, team2, team1Goal - etGoals[0], team2Goal - etGoals[1], true, true, etGoals[0], etGoals[1]);
			
			if (team1Goal == team2Goal) {				
				int[] result = penalties(team1, team2, team1Pens, team2Pens, false);
				team1Pens = result[0];
				team2Pens = result[1];
				if (result[2] == 1) Winner = team1;
				else Winner = team2;
				Winner = winner(team1, team2, team1Goal - etGoals[0], team2Goal - etGoals[1], false, etGoals[0], etGoals[1], false, team1Pens, team2Pens, true);
				JLabel pensResults = createLabel("", textFont, null);
				if (Winner.equals(team1)) {
					pensResults.setText(team1 + " wins " + team1Pens + "-" + team2Pens + " on penalties!");
					if (team1.equals(userTeam.getName())) {
						pensResults.setFont(userTeamFont.deriveFont(45f));
						playSound("crowdCheer.wav");
					} else playSound("crowdBoo.wav");
				} else {
					pensResults.setText(team2 + " wins " + team2Pens + "-" + team1Pens + " on penalties!");
					if (team2.equals(userTeam.getName())) {
						pensResults.setFont(userTeamFont.deriveFont(45f));
						playSound("crowdCheer.wav");
					} else playSound("crowdBoo.wav");
				}
				koGamesResults.add(pensResults);
			} else {
				JLabel etResults = createLabel("", textFont, null);
				Winner = winner(team1, team2, team1Goal - etGoals[0], team2Goal - etGoals[1], false, etGoals[0], etGoals[1], true, 0, 0, false);
				if (Winner.equals(team1)) {
					etResults.setText(team1 + " wins in extra time!");
					if (team1.equals(userTeam.getName())) {
						etResults.setFont(userTeamFont.deriveFont(45f));
						playSound("crowdCheer.wav");
					} else playSound("crowdBoo.wav");
				} else {
					etResults.setText(team2 + " wins in extra time!");
					if (team2.equals(userTeam.getName())) {
						etResults.setFont(userTeamFont.deriveFont(45f));
						playSound("crowdCheer.wav");
					} else playSound("crowdBoo.wav");
				}
				koGamesResults.add(etResults);
			}
		} else {
			displayMatchReport(team1, team2, team1Goal, team2Goal, true, false, 0, 0);
			JLabel koResults = createLabel("", textFont, null);
			Winner = winner(team1, team2, team1Goal, team2Goal, false, 0, 0, false, 0, 0, false);
			if (Winner.equals(team1)) {
				koResults.setText(team1 + " wins in regular time!");
				if (team1.equals(userTeam.getName())) {
					koResults.setFont(userTeamFont.deriveFont(45f));
					playSound("crowdCheer.wav");
				} else playSound("crowdBoo.wav");
			} else {
				koResults.setText(team2 + " wins in regular time!");
				if (team2.equals(userTeam.getName())) {
					koResults.setFont(userTeamFont.deriveFont(45f));
					playSound("crowdCheer.wav");
				} else playSound("crowdBoo.wav");
			}
			koGamesResults.add(koResults);
		}
		
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 2);
		if (KOcount != KOlimit){
			userPlayedKOgame = true;
			JButton nextGameButton = createButton("Next Game", "nextGame", choiceHandler, buttonFont);
			buttonPanel.add(nextGameButton);
		} else if (KOcount == KOlimit) {
			JButton nextStageButton = createButton("Next Stage", "checkProgressed", choiceHandler, buttonFont);
			if (KOmatches == 3) {
				nextStageButton.setText("Finish");
				nextStageButton.setActionCommand("nextStage");
			}
			buttonPanel.add(nextStageButton);
		}
		if (KOcount > 0 && KOmatches != 3) {
			JButton previousGameButton = createButton("Previous Game", "previousGame", choiceHandler, buttonFont);
			buttonPanel.add(previousGameButton);
		}
		
		JButton exitGameButton = createButton("Exit Game", "exit", choiceHandler, buttonFont);
		buttonPanel.add(exitGameButton);
		buttonPanel = addEmptySpaces(buttonPanel, 2);
		
		teams = retrieveData(15, false, false);
		for (Team t : teams)
			if (t.getName().equals(Winner)) progressed.add(t);
		
		con.add(koGamesResults, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// simulate the other given knock-out stage matches
	public String bgKnockoutGame(String team1, String team2, int team1Goal, int team2Goal, boolean sim) {
		Random rand = new Random();
		int team1Pens = 0; int team2Pens = 0;
		int team1Rank = 0; int team2Rank = 0;
		String Winner = "";
		teams = retrieveData(15, false, false);
		
		for (Team t : teams) {
			if (t.getName().equals(team1)) team1Rank = t.getWorldRank();
			else if (t.getName().equals(team2)) team2Rank = t.getWorldRank();
		}
		
		int[] goals = goal(team1Rank, team2Rank, team1, team2, false);
		team1Goal = goals[0];
		team2Goal = goals[1];
		
		if (!sim) {
			resetWindow();
			
			String stadiumName = stadiums.get(rand.nextInt(8));
			JPanel koGameHeader = createPanel(new GridLayout(2, 1), 0);
			JLabel gameLabel = createLabel("", textFont, null);
			gameLabel.setBorder(new EmptyBorder(25, 0, 5, 0));
			if (KOmatches == 0) gameLabel.setText("ROUND OF 16 GAME " + (KOcount + 1));
			if (KOmatches == 1) gameLabel.setText("QUARTER FINAL GAME " + (KOcount + 1));
			if (KOmatches == 2) gameLabel.setText("SEMI FINAL GAME " + (KOcount + 1));
			if (KOmatches == 3) gameLabel.setText("WORLD CUP FINAL");
			koGameHeader.add(gameLabel);
			JLabel stadiumHeader = createLabel(stadiumName, buttonFont, null);
			stadiumHeader.setBorder(new EmptyBorder(5, 0, 25, 0));
			koGameHeader.add(stadiumHeader);
			con.add(koGameHeader, BorderLayout.NORTH);
			
			koGamesResults = createPanel(new GridLayout(5, 1), 0);
			JPanel KOgame = createPanel(new GridLayout(1, 5), 0);
			JLabel KOhTeam = createLabel(team1, textFont, null);
			KOgame.add(KOhTeam);
			JLabel KOhTeamScore = createLabel(Integer.toString(team1Goal), textFont, null);
			KOgame.add(KOhTeamScore);
			JLabel vsLabel = createLabel(" - ", textFont, null);
			KOgame.add(vsLabel);
			JLabel KOaTeamScore = createLabel(Integer.toString(team2Goal), textFont, null);
			KOgame.add(KOaTeamScore);
			JLabel KOaTeam = createLabel(team2, textFont, null);
			KOgame.add(KOaTeam);
			koGamesResults.add(KOgame);
		}
		
		JLabel winnerLabel = createLabel("", textFont, null);
		if (team1Goal == team2Goal) {
			int[] etGoals = goal(team1Rank, team2Rank, team1, team2, true);
			team1Goal += etGoals[0];
			team2Goal += etGoals[1];
			
			if (!sim) {
				JLabel etResultLabel = createLabel("FINAL RESULT (AFTER EXTRA TIME):", textFont, null);
				koGamesResults.add(etResultLabel);
				JPanel etKOgame = createPanel(new GridLayout(1, 5), 0);
				JLabel etKOhTeam = createLabel(team1, textFont, null);
				etKOgame.add(etKOhTeam);
				JLabel etKOhTeamScore = createLabel(Integer.toString(team1Goal), textFont, null);
				etKOgame.add(etKOhTeamScore);
				JLabel etVsLabel = createLabel(" - ", textFont, null);
				etKOgame.add(etVsLabel);
				JLabel etKOaTeamScore = createLabel(Integer.toString(team2Goal), textFont, null);
				etKOgame.add(etKOaTeamScore);
				JLabel etKOaTeam = createLabel(team2, textFont, null);
				etKOgame.add(etKOaTeam);
				koGamesResults.add(etKOgame);
			}
			
			if (team1Goal == team2Goal) {
				int[] result  = penalties(team1, team2, team1Pens, team2Pens, sim);
				team1Pens = result[0];
				team2Pens = result[1];
				Winner = winner(team1, team2, team1Goal - etGoals[0], team2Goal - etGoals[1], false, etGoals[0], etGoals[1], false, team1Pens, team2Pens, true);
				if (result[2] == 1) Winner = team1;
				else Winner = team2;
				if (!sim) {
					if (Winner.equals(team1)) winnerLabel.setText(team1 + " wins " + team1Pens + "-" + team2Pens + " on penalties!");
					else winnerLabel.setText(team2 + " wins " + team2Pens + "-" + team1Pens + " on penalties!");
				}
			} else {
				Winner = winner(team1, team2, team1Goal - etGoals[0], team2Goal - etGoals[1], false, etGoals[0], etGoals[1], true, 0, 0, false);
				if (!sim) winnerLabel.setText(Winner + " wins in extra time!");
			}
		} else {
			Winner = winner(team1, team2, team1Goal, team2Goal, false, 0, 0, false, 0, 0, false);
			if (!sim) winnerLabel.setText(Winner + " wins in regular time!");
		}
		
		if (!sim) {
			koGamesResults.add(winnerLabel);
			con.add(koGamesResults, BorderLayout.CENTER);
			koGamesResults.setVisible(true);
		}
		
		return Winner;
	}
	
	// simulate a penalty shoot-out between the given teams
	public int[] penalties(String team1, String team2, int team1Pens, int team2Pens, boolean sim) {		
		boolean team1Penalty, team2Penalty;
		double pressure = 0.1;
		int suddenDeathCounter = 0;
		
		for (int i = 1; i <= 5; i++) {
			pressure = pressure * i;
			
			team1Penalty = scoringPenalty(pressure);
			if (team1Penalty) team1Pens++;
			if (((team1Pens - team2Pens) > (5 - i + 1)) || (team2Pens - team1Pens) > (5 - i + 1)) i = 10;
			
			if (i < 9) {
				team2Penalty = scoringPenalty(pressure);
				if (team2Penalty) team2Pens++;
				if (((team1Pens - team2Pens) > (5 - i)) || (team2Pens - team1Pens) > (5 - i)) i = 10;
			}
		}
		
		if (team1Pens == team2Pens) {
			pressure = 0.1;
			
			while (team1Pens == team2Pens) {
				suddenDeathCounter++;
				pressure = pressure * suddenDeathCounter;
				
				team1Penalty = scoringPenalty(pressure);
				if (team1Penalty) team1Pens++;
					
				team2Penalty = scoringPenalty(pressure);
				if (team2Penalty) team2Pens++;
			}
		}
				
		if (team1Pens > team2Pens) return new int[] {team1Pens, team2Pens, 1};
		else return new int[] {team1Pens, team2Pens, 2};
	}
	
	// simulate scoring a penalty
	public boolean scoringPenalty(double pressure) {
		double chance = 0.715;
		boolean penaltyScored;
		SecureRandom srand = new SecureRandom();
		chance = chance - pressure;
		
		if (chance < 0.1) chance = 0.1;
		
		double penaltyAttempt = srand.nextDouble();
		if (penaltyAttempt <= chance) penaltyScored = true;
		else penaltyScored = false;
		
		return penaltyScored;
	}
	
	// check whether the user won/lost the final, and display the appropriate screen
	public void winninglosingWC(boolean userWin) {
		resetWindow();
		
		if (!continueSim) {
			JLabel WCwinner = createLabel("You have won the World Cup with " + userTeam.getName() + ". Congratulations!", textFont, null);
			WCwinner.setBorder(new EmptyBorder(25, 0, 0, 0));
			if (!userWin) {
				WCwinner.setText("Unlucky, you lost in the World Cup final with " + userTeam.getName() + ".");
				JLabel losingGif = playGIF("losing.gif");
				con.add(losingGif, BorderLayout.CENTER);
				if (clip != null) {
					clip.stop();
					playSound("crowdBoo.wav");
				}
			}
			else {
				JLabel winningGif = playGIF("winning.gif");
				con.add(winningGif, BorderLayout.CENTER);
				if (clip != null) {
					clip.stop();
					playSound("crowdCheer.wav");
				}
			}
			
			JPanel buttonPanel = createPanel(new GridLayout(1, 7), 3);
			JButton nextButton = createButton("Check Stats", "checkStats", choiceHandler, buttonFont);
			buttonPanel.add(nextButton);
			buttonPanel = addEmptySpaces(buttonPanel, 3);
			
			exitLabel = WCwinner;
			con.add(WCwinner, BorderLayout.NORTH);
			con.add(buttonPanel, BorderLayout.SOUTH);
		} else gettingUserRating();
	}
	
	// display the stats of the user's team and their ranking amongst the other teams in those stats
	public void statsPage(JLabel exitLabel) {
		resetWindow();
		
		DecimalFormat df = new DecimalFormat("0.0");
		Team temp = new Team("", "", 0, "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
		teams = retrieveData(15, false, false);
		
		for (Team t : teams)
			if (t.getName().equals(userTeam.getName())) temp = t;
		
		JPanel endPanel = createPanel(new GridLayout(10, 1), 1);
		JLabel gamesPlayed = createLabel("You played " + temp.getGPlayed() + " games. (Rank: " + gettingTeamRank(1) + ")", textFont, null);
		endPanel.add(gamesPlayed);
		JLabel gamesWon = createLabel("You won " + temp.getGWon() + " games (Rank: " + gettingTeamRank(2) + ") (" + temp.getETWon() + " in extra time (" + gettingTeamRank(9) + ") & " + temp.getPenWon() + " on penalties (" + gettingTeamRank(12) + "))", textFont, null);
		if (temp.getGWon() == 1) gamesWon.setText("You won " + temp.getGWon() + " game (Rank: " + gettingTeamRank(2) + ") (" + temp.getETWon() + " in extra time (" + gettingTeamRank(9) + ") & " + temp.getPenWon() + " on penalties (" + gettingTeamRank(12) + "))");
		endPanel.add(gamesWon);
		JLabel gamesDrawn = createLabel("You drew " + temp.getGDrawn() + " games (Rank: " + gettingTeamRank(3) + ") (" + temp.getETDrawn() + " in extra time (" + gettingTeamRank(10) + "))", textFont, null);
		if (temp.getGDrawn() == 1) gamesWon.setText("You drew " + temp.getGDrawn() + " games (Rank: " + gettingTeamRank(3) + ") (" + temp.getETDrawn() + " in extra time (" + gettingTeamRank(10) + "))");
		endPanel.add(gamesDrawn);
		JLabel gamesLost = createLabel("You lost " + temp.getGLost() + " game (Rank: " + gettingTeamRank(4) + ") (" + temp.getETLost() + " in extra time (" + gettingTeamRank(11) + ") & " + temp.getPenLost() + " on penalties (" + gettingTeamRank(13) + "))", textFont, null);
		if (temp.getGLost() == 1) gamesLost.setText("You lost " + temp.getGLost() + " game (Rank: " + gettingTeamRank(4) + ") (" + temp.getETLost() + " in extra time (" + gettingTeamRank(11) + ") & " + temp.getPenLost() + " on penalties (" + gettingTeamRank(13) + "))");
		endPanel.add(gamesLost);
		JLabel totalPoints = createLabel("You earned " + temp.getTotalPoints() + " points in the group stage (avg. " + df.format(((float) temp.getTotalPoints())/3) + "pg). (Rank: " + gettingTeamRank(8) + ")", textFont, null);
		if (temp.getTotalPoints() == 1) totalPoints.setText("You earned " + temp.getTotalPoints() + " point in the group stage (avg. " + df.format(((float) temp.getTotalPoints())/3) + "pg). (Rank: " + gettingTeamRank(8) + ")");
		endPanel.add(totalPoints);
		JLabel goalsScored = createLabel("You scored " + temp.getGoalsScored() + " goals (avg. " + df.format(((float) temp.getGoalsScored())/temp.getGPlayed()) + "pg). (Rank: " + gettingTeamRank(5) + ")", textFont, null);
		if (temp.getGoalsScored() == 1) goalsScored.setText("You scored " + temp.getGoalsScored() + " goal (avg. " + df.format(((float) temp.getGoalsScored())/temp.getGPlayed()) + "pg). (Rank: " + gettingTeamRank(5) + ")");
		endPanel.add(goalsScored);
		JLabel goalsConceded = createLabel("You conceded " + temp.getGoalsConceded() + " goals (avg. " + df.format(((float) temp.getGoalsConceded())/temp.getGPlayed()) + "pg). (Rank: " + gettingTeamRank(6) + ")", textFont, null);
		if (temp.getGoalsConceded() == 1) goalsConceded.setText("You conceded " + temp.getGoalsConceded() + " goal (avg. " + df.format(((float) temp.getGoalsConceded())/temp.getGPlayed()) + "pg). (Rank: " + gettingTeamRank(6) + ")");
		endPanel.add(goalsConceded);
		JLabel goalDiff = createLabel("You had a goal diff. of " + temp.getGoalDiff() + ". (Rank: " + gettingTeamRank(7) + ")", textFont, null);
		endPanel.add(goalDiff);
		
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 2);
		JButton rateSimButton = createButton("Rate Sim", "rateSim", choiceHandler, buttonFont);
		buttonPanel.add(rateSimButton);
		if (KOmatches < 3) {
			JButton finishSimButton = createButton("Finish Sim", "finishSim", choiceHandler, buttonFont);
			buttonPanel.add(finishSimButton);
		}
		buttonPanel = addEmptySpaces(buttonPanel, 2);
		
		con.add(exitLabel, BorderLayout.NORTH);
		con.add(endPanel, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// get the rank of the user's team in the given stat category
	public int gettingTeamRank(int teamStat) {
		int rank = 0;
		List<Team> allTeams = retrieveData(teamStat, false, true);
		
		for (Team t : allTeams)
			if (t.getName().equals(userTeam.getName())) rank = allTeams.indexOf(t) + 1;
		
		return rank;
	}
	
	// check whether there is a saved file
	public void checkingForSavedGame() {
		File f = new File(fileName);
		try {
			if (f.exists()) loadingGame();
			else {
				resetWindow();
				JLabel noSavedGame = createLabel("There is no saved game", textFont, null);
				JPanel buttonPanel = createPanel(new GridLayout(1, 7), 3);
				JButton backButton = createButton("Back", "loadGame", choiceHandler, buttonFont);
				buttonPanel.add(backButton);
				buttonPanel = addEmptySpaces(buttonPanel, 3);
				con.add(noSavedGame, BorderLayout.CENTER);
				con.add(buttonPanel, BorderLayout.SOUTH);
			}
		} catch (Exception e) {
			errorScreen(e.getMessage());
		}
	}
	
	// load the saved file
	public void loadingGame() {
		userTeam = new Team("", "", 0, "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
		List<String> temp = new ArrayList<String>();
		List<Team> savedTeams = new ArrayList<Team>(32);
		List<String> userGroupString = new ArrayList<String>(4);
		progressed = new ArrayList<Team>();
		playedAgainst = new ArrayList<String>();
		userTeamString = "";
        matches = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
        	userTeamString = br.readLine();
        	matches = Integer.parseInt(br.readLine());
        	groupCount = Integer.parseInt(br.readLine());
        	KOmatches = Integer.parseInt(br.readLine());
        	KOcount = Integer.parseInt(br.readLine());
        	KOlimit = Integer.parseInt(br.readLine());
        	
        	for (int i = 0; i < 32; i++) {
        		Team t = new Team(br.readLine(), br.readLine(), Integer.parseInt(br.readLine()), br.readLine(), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), 0, Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Double.parseDouble(br.readLine()), Double.parseDouble(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Double.parseDouble(br.readLine()), Double.parseDouble(br.readLine()), Double.parseDouble(br.readLine()), Integer.parseInt(br.readLine()));
        		if (t.getName().equals(userTeamString)) userTeam = t;
        		savedTeams.add(t);
        		updateData(t.getName(), t.getGPlayed(), t.getGWon(), t.getGDrawn(), t.getGLost(), t.getTotalPoints(), t.getETWon(), t.getETDrawn(), t.getETLost(), t.getPenWon(), t.getPenLost(), t.getTeamMorale(), t.getPredRisk());
        		updateGoal(t.getName(), t.getGPlayed(), t.getGoalsScored(), t.getGoalsConceded(), t.getGoalDiff(), t.getAvgGS(), t.getAvgGC());
        	}
        	
        	int count = Integer.parseInt(br.readLine());
        	for (int i = 0; i < count; i++) {
        		Team t = new Team(br.readLine(), br.readLine(), Integer.parseInt(br.readLine()), br.readLine(), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), 0, Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Double.parseDouble(br.readLine()), Double.parseDouble(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine()), Double.parseDouble(br.readLine()), Double.parseDouble(br.readLine()), Double.parseDouble(br.readLine()), Integer.parseInt(br.readLine()));
        		progressed.add(t);
        	}
        	
        	for (int i = 0; i < 4; i++) temp.add(br.readLine());
        } catch (IOException e) {
        	errorScreen(e.getMessage());
        }
        
        if (temp.size() > 0) {
        	if (temp.get(0).isEmpty()) {
        		for (int i = 0; i < 32; i++)
                    if (savedTeams.get(i).getGroup().equals(userTeam.getGroup())) userGroupString.add(savedTeams.get(i).getName());
        	} else {
        		for (int i = 1; i < temp.size(); i++)
        			playedAgainst.add(temp.get(i));
        	}
        }
        
        mode = "classic";
        
        if (matches < 3) simulatingGroupStages(true);
        else simulatingKnockoutStages(true);
	}
	
	// save the user's progress into a file
	public void saveGame(boolean overwrite) {
		teams = retrieveData(15, false, false);
		
		try {
			File f = new File(fileName);
			if (f.exists() && !overwrite) overwriteSaveFile();
			else {
				if (overwrite) f.delete();
				f.createNewFile();
				try (PrintWriter pw = new PrintWriter(fileName)) {
					pw.println(userTeam.getName());
					pw.println(matches);
					pw.println(groupCount);
					pw.println(KOmatches);
					pw.println(KOcount);
					pw.println(KOlimit);
						
					for (Team t : teams) {
						pw.println(t.getName());
						pw.println(t.getShort());
						pw.println(t.getWorldRank());
						pw.println(t.getGroup());
						pw.println(t.getGPlayed());
						pw.println(t.getGWon());
						pw.println(t.getGDrawn());
						pw.println(t.getGLost());
						pw.println(t.getGoalsScored());
						pw.println(t.getGoalsConceded());
						pw.println(t.getGoalDiff());
						pw.println(t.getTotalPoints());
						pw.println(t.getETWon());
						pw.println(t.getETDrawn());
						pw.println(t.getETLost());
						pw.println(t.getPenWon());
						pw.println(t.getPenLost());
						pw.println(t.getTeamMorale());
						pw.println(t.getAvgGS());
						pw.println(t.getAvgGC());
						pw.println(t.getQualiMP());
						pw.println(t.getQualiGS());
						pw.println(t.getQualiGC());
						pw.println(t.getAvgC());
						pw.println(t.getXGF());
						pw.println(t.getXGA());
						pw.println(t.getPredRisk());
					}
						
					pw.println(progressed.size());
					for (Team i : progressed) {
						pw.println(i.getName());
						pw.println(i.getShort());
						pw.println(i.getWorldRank());
						pw.println(i.getGroup());
						pw.println(i.getGPlayed());
						pw.println(i.getGWon());
						pw.println(i.getGDrawn());
						pw.println(i.getGLost());
						pw.println(i.getGoalsScored());
						pw.println(i.getGoalsConceded());
						pw.println(i.getGoalDiff());
						pw.println(i.getTotalPoints());
						pw.println(i.getETWon());
						pw.println(i.getETDrawn());
						pw.println(i.getETLost());
						pw.println(i.getPenWon());
						pw.println(i.getPenLost());
						pw.println(i.getTeamMorale());
						pw.println(i.getAvgGS());
						pw.println(i.getAvgGC());
						pw.println(i.getQualiMP());
						pw.println(i.getQualiGS());
						pw.println(i.getQualiGC());
						pw.println(i.getAvgC());
						pw.println(i.getXGF());
						pw.println(i.getXGA());
						pw.println(i.getPredRisk());
					}
						
					if (playedAgainst.size() > 0) {
						pw.println(playedAgainst.size());
						for (var j : playedAgainst)
							pw.println(j);
					} else pw.println();
					
					pw.close();
				}
				
				saveGameMsg();
			}
		} catch (IOException e) {
			errorScreen(e.getMessage());
		}
	}
	
	// ask the user if they want to overwrite a saved file
	public void overwriteSaveFile() {
		resetWindow();
		
		JLabel overwriteFile = createLabel("There's already a file saved there, do you want to overwrite it?", textFont, null);
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 2);
		JButton yesButton = createButton("Yes", "overwriteYes", choiceHandler, buttonFont);
		buttonPanel.add(yesButton);
		JButton noButton = createButton("No", "overwriteNo", choiceHandler, buttonFont);
		buttonPanel.add(noButton);
		buttonPanel = addEmptySpaces(buttonPanel, 2);
		
		con.add(overwriteFile, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}

	// get the number of simulations to run from the user
	public void gettingNoOfSims() {
		resetWindow();
		
		JPanel simsPanel = createPanel(new GridLayout(4, 1), 1);
		JLabel simsLabel = createLabel("HOW MANY SIMULATIONS WOULD YOU LIKE TO RUN?", textFont, null);
		simsLabel.setBorder(new EmptyBorder(25, 0, 0, 0));
		
		slider = new JSlider(0, 100, 0);
		slider.setForeground(Color.white);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.addChangeListener(sliderChanger);
		slider.setPreferredSize(new Dimension(1000, 100));
		slider.setFont(statFont);
		
		getUserInput = new JTextField("0");
		getUserInput.setHorizontalAlignment(SwingConstants.CENTER);
		getUserInput.setEditable(false);
		getUserInput.setPreferredSize(new Dimension(200, 50));
		getUserInput.setFont(statFont);
		
		JPanel sliderWrapper = new JPanel(new FlowLayout());
		sliderWrapper.setBackground(new Color(35, 2, 19));
		sliderWrapper.add(slider);
		JPanel ratingWrapper = new JPanel(new FlowLayout());
		ratingWrapper.setBackground(new Color(35, 2, 19));
		ratingWrapper.add(getUserInput);
		
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 3);
		JButton backButton = createButton("Back", "mainMenu", choiceHandler, buttonFont);
		buttonPanel.add(backButton);
		JButton resetButton = createButton("Reset", "resetSlider2", choiceHandler, buttonFont);
		buttonPanel.add(resetButton);
		JButton continueButton = createButton("Continue", "simTest", choiceHandler, buttonFont);
		buttonPanel.add(continueButton);
		buttonPanel = addEmptySpaces(buttonPanel, 3);
		
		simsPanel.add(sliderWrapper);
		simsPanel.add(ratingWrapper);
		con.add(simsLabel, BorderLayout.NORTH);
		con.add(simsPanel, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// run the given amount of simulations
	public void simulatingTournament() {
		String hTeam, aTeam, Winner;
		int team1Goal = 0, team2Goal = 0;
		int[] goals;
		List<Team> teams = retrieveData(15, false, false);
		List<Team> groupA = new ArrayList<Team>(); List<Team> groupB = new ArrayList<Team>(); List<Team> groupC = new ArrayList<Team>(); List<Team> groupD = new ArrayList<Team>();
		List<Team> groupE = new ArrayList<Team>(); List<Team> groupF = new ArrayList<Team>(); List<Team> groupG = new ArrayList<Team>(); List<Team> groupH = new ArrayList<Team>();
		List<Team> progressed = new ArrayList<Team>();
		
		for (Team t : teams) {
			clearData(t.getName(), false);
			
			if (t.getGroup().equals("A")) groupA.add(t);
			else if (t.getGroup().equals("B")) groupB.add(t);
			else if (t.getGroup().equals("C")) groupC.add(t);
			else if (t.getGroup().equals("D")) groupD.add(t);
			else if (t.getGroup().equals("E")) groupE.add(t);
			else if (t.getGroup().equals("F")) groupF.add(t);
			else if (t.getGroup().equals("G")) groupG.add(t);
			else if (t.getGroup().equals("H")) groupH.add(t);
		}	
		List<List<Team>> groups = Arrays.asList(groupA, groupB, groupC, groupD, groupE, groupF, groupG, groupH);
		
		for (int n = 0; n < noOfSims; n++) {
			for (int i = 0; i < groups.size(); i++) {
				hTeam = generatingHomeTeam(groups.get(i).get(0).getName(), groups.get(i).get(1).getName());
				aTeam = generatingAwayTeam(groups.get(i).get(0).getName(), groups.get(i).get(1).getName(), hTeam);
				goals = bgGroupStageGame(hTeam, aTeam, team1Goal, team2Goal);
				team1Goal = goals[0];
				team2Goal = goals[1];
				winner(hTeam, aTeam, team1Goal, team2Goal, true, 0, 0, false, 0, 0, false);
				team1Goal = 0; team2Goal = 0; goals = new int[2];
				
				hTeam = generatingHomeTeam(groups.get(i).get(2).getName(), groups.get(i).get(3).getName());
				aTeam = generatingAwayTeam(groups.get(i).get(2).getName(), groups.get(i).get(3).getName(), hTeam);
				goals = bgGroupStageGame(hTeam, aTeam, team1Goal, team2Goal);
				team1Goal = goals[0];
				team2Goal = goals[1];
				winner(hTeam, aTeam, team1Goal, team2Goal, true, 0, 0, false, 0, 0, false);
				team1Goal = 0; team2Goal = 0; goals = new int[2];
				
				hTeam = generatingHomeTeam(groups.get(i).get(0).getName(), groups.get(i).get(2).getName());
				aTeam = generatingAwayTeam(groups.get(i).get(0).getName(), groups.get(i).get(2).getName(), hTeam);
				goals = bgGroupStageGame(hTeam, aTeam, team1Goal, team2Goal);
				team1Goal = goals[0];
				team2Goal = goals[1];
				winner(hTeam, aTeam, team1Goal, team2Goal, true, 0, 0, false, 0, 0, false);
				team1Goal = 0; team2Goal = 0; goals = new int[2];
				
				hTeam = generatingHomeTeam(groups.get(i).get(1).getName(), groups.get(i).get(3).getName());
				aTeam = generatingAwayTeam(groups.get(i).get(1).getName(), groups.get(i).get(3).getName(), hTeam);
				goals = bgGroupStageGame(hTeam, aTeam, team1Goal, team2Goal);
				team1Goal = goals[0];
				team2Goal = goals[1];
				winner(hTeam, aTeam, team1Goal, team2Goal, true, 0, 0, false, 0, 0, false);
				team1Goal = 0; team2Goal = 0; goals = new int[2];
				
				hTeam = generatingHomeTeam(groups.get(i).get(0).getName(), groups.get(i).get(3).getName());
				aTeam = generatingAwayTeam(groups.get(i).get(0).getName(), groups.get(i).get(3).getName(), hTeam);
				goals = bgGroupStageGame(hTeam, aTeam, team1Goal, team2Goal);
				team1Goal = goals[0];
				team2Goal = goals[1];
				winner(hTeam, aTeam, team1Goal, team2Goal, true, 0, 0, false, 0, 0, false);
				team1Goal = 0; team2Goal = 0; goals = new int[2];
				
				hTeam = generatingHomeTeam(groups.get(i).get(1).getName(), groups.get(i).get(2).getName());
				aTeam = generatingAwayTeam(groups.get(i).get(1).getName(), groups.get(i).get(2).getName(), hTeam);
				goals = bgGroupStageGame(hTeam, aTeam, team1Goal, team2Goal);
				team1Goal = goals[0];
				team2Goal = goals[1];
				winner(hTeam, aTeam, team1Goal, team2Goal, true, 0, 0, false, 0, 0, false);
				team1Goal = 0; team2Goal = 0; goals = new int[2];
				
				groups.set(i, sortingSimulatedGroups(groups.get(i)));
				progressed.add(groups.get(i).get(0));
				progressed.add(groups.get(i).get(1));
			}

			List<Team> R16_1 = Arrays.asList(progressed.get(0), progressed.get(3));
			List<Team> R16_2 = Arrays.asList(progressed.get(4), progressed.get(7));
			List<Team> R16_3 = Arrays.asList(progressed.get(8), progressed.get(11));
			List<Team> R16_4 = Arrays.asList(progressed.get(12), progressed.get(15));
			List<Team> R16_5 = Arrays.asList(progressed.get(2), progressed.get(1));
			List<Team> R16_6 = Arrays.asList(progressed.get(6), progressed.get(5));
			List<Team> R16_7 = Arrays.asList(progressed.get(10), progressed.get(9));
			List<Team> R16_8 = Arrays.asList(progressed.get(14), progressed.get(13));
			List<List<Team>> allR16s = Arrays.asList(R16_1, R16_2, R16_3, R16_4, R16_5, R16_6, R16_7, R16_8);
			progressed.clear();
			for (int i = 0; i < allR16s.size(); i++) {
				hTeam = generatingHomeTeam(allR16s.get(i).get(0).getName(), allR16s.get(i).get(1).getName());
				aTeam = generatingAwayTeam(allR16s.get(i).get(0).getName(), allR16s.get(i).get(1).getName(), hTeam);
				Winner = bgKnockoutGame(hTeam, aTeam, team1Goal, team2Goal, true);
				if (Winner.equals(allR16s.get(i).get(0).getName())) progressed.add(allR16s.get(i).get(0));
				else progressed.add(allR16s.get(i).get(1));
				team1Goal = 0; team2Goal = 0;
			}

			List<Team> QF_1 = Arrays.asList(progressed.get(0), progressed.get(1));
			List<Team> QF_2 = Arrays.asList(progressed.get(2), progressed.get(3));
			List<Team> QF_3 = Arrays.asList(progressed.get(4), progressed.get(5));
			List<Team> QF_4 = Arrays.asList(progressed.get(6), progressed.get(7));
			List<List<Team>> allQFs = Arrays.asList(QF_1, QF_2, QF_3, QF_4);
			progressed.clear();
			for (int i = 0; i < allQFs.size(); i++) {
				hTeam = generatingHomeTeam(allQFs.get(i).get(0).getName(), allQFs.get(i).get(1).getName());
				aTeam = generatingAwayTeam(allQFs.get(i).get(0).getName(), allQFs.get(i).get(1).getName(), hTeam);
				Winner = bgKnockoutGame(hTeam, aTeam, team1Goal, team2Goal, true);
				if (Winner.equals(allQFs.get(i).get(0).getName())) progressed.add(allQFs.get(i).get(0));
				else progressed.add(allQFs.get(i).get(1));
				team1Goal = 0; team2Goal = 0;
			}

			List<Team> SF_1 = Arrays.asList(progressed.get(0), progressed.get(1));
			List<Team> SF_2 = Arrays.asList(progressed.get(2), progressed.get(3));
			List<List<Team>> allSFs = Arrays.asList(SF_1, SF_2);
			progressed.clear();
			for (int i = 0; i < allSFs.size(); i++) {
				hTeam = generatingHomeTeam(allSFs.get(i).get(0).getName(), allSFs.get(i).get(1).getName());
				aTeam = generatingAwayTeam(allSFs.get(i).get(0).getName(), allSFs.get(i).get(1).getName(), hTeam);
				Winner = bgKnockoutGame(hTeam, aTeam, team1Goal, team2Goal, true);
				if (Winner.equals(allSFs.get(i).get(0).getName())) progressed.add(allSFs.get(i).get(0));
				else progressed.add(allSFs.get(i).get(1));
				team1Goal = 0; team2Goal = 0;
			}

			team1Goal = 0; team2Goal = 0;
			hTeam = generatingHomeTeam(progressed.get(0).getName(), progressed.get(1).getName());
			aTeam = generatingAwayTeam(progressed.get(0).getName(), progressed.get(1).getName(), hTeam);
			String WCwinner = bgKnockoutGame(hTeam, aTeam, team1Goal, team2Goal, true);
			if (WCwinner.equals(progressed.get(0).getName())) {
				progressed.get(0).setTimesWon(progressed.get(0).getTimesWon() + 1);
				updateWins(progressed.get(0).getName(), progressed.get(0).getTimesWon());
			} else {
				progressed.get(1).setTimesWon(progressed.get(1).getTimesWon() + 1);
				updateWins(progressed.get(1).getName(), progressed.get(1).getTimesWon());
			}
			progressed.clear();
			for (Team t : teams)
				clearData(t.getName(), true);
		}
		
		displayingSimWinners(0, false);
	}
	
	// update the times won for the given team
	public void updateWins(String teamName, int timesWon) {
		String query = "UPDATE WCTeams SET TimesWon = ? WHERE Name = ?";
		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, timesWon);
			pstmt.setString(2, teamName);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			errorScreen(e.getMessage());
		}
	}
	
	// clean the data in the database between each simulation
	public void clearData(String teamName, boolean runningSims) {
		String query = "";
		String[] teamDataTokens = parseCSVData(teamName);
		if (runningSims) query = "UPDATE WCTeams SET TeamMorale = 50, AvgGS = ?, AvgGC = ?, predRisk = ? WHERE Name = ?";
		else query = "UPDATE WCTeams SET GamesPlayed = 0, GamesWon = 0, GamesDrawn = 0, GamesLost = 0, GoalsScored = 0, GoalsConceded = 0, GoalDifference = 0, TotalPoints = 0, TimesWon = 0, ETWon = 0, ETDrawn = 0, ETLost = 0, PenWon = 0, PenLost = 0, TeamMorale = 50, AvgGS = ?, AvgGC = ?, predRisk = ? WHERE Name = ?";

		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setDouble(1, Double.parseDouble(teamDataTokens[1]));
			pstmt.setDouble(2, Double.parseDouble(teamDataTokens[2]));
			pstmt.setDouble(3, Double.parseDouble(teamDataTokens[9]));
			pstmt.setString(4, teamName);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			errorScreen(e.getMessage());
		}
	}

	// display the top/bottom 10 teams from each stat category
	public void displayingSimWinners(int statsInt, boolean reverse) {
		DecimalFormat df = new DecimalFormat("0.0");
		List<Team> simTWonTeams = retrieveData(0, false, false); List<Team> simGWonTeams = retrieveData(2, false, false); List<Team> simGScoredTeams = retrieveData(5, false, false);
		List<Team> simGPlayedTeams = retrieveData(1, false, false); List<Team> simGDrawnTeams = retrieveData(3, false, false); List<Team> simGLostTeams = retrieveData(4, true, false);
		List<Team> simGoalsConcededTeams = retrieveData(6, true, false); List<Team> simGoalDiffTeams = retrieveData(7, false, false); List<Team> simTotalPointsTeams = retrieveData(8, false, false);
		List<Team> simETWonTeams = retrieveData(9, false, false); List<Team> simETDrawnTeams = retrieveData(10, false, false); List<Team> simETLostTeams = retrieveData(11, false, false);
		List<Team> simPenWonTeams = retrieveData(12, false, false); List<Team> simPenLostTeams = retrieveData(13, false, false);
		
		if (reverse) {
			simTWonTeams = retrieveData(0, true, false); simGWonTeams = retrieveData(2, true, false); simGScoredTeams = retrieveData(5, true, false);
			simGPlayedTeams = retrieveData(1, true, false); simGDrawnTeams = retrieveData(3, true, false); simGLostTeams = retrieveData(4, false, false);
			simGoalsConcededTeams = retrieveData(6, false, false); simGoalDiffTeams = retrieveData(7, true, false); simTotalPointsTeams = retrieveData(8, true, false);
			simETWonTeams = retrieveData(9, true, false); simETDrawnTeams = retrieveData(10, true, false); simETLostTeams = retrieveData(11, true, false);
			simPenWonTeams = retrieveData(12, true, false); simPenLostTeams = retrieveData(13, true, false);
		}
				
		resetWindow();
		
		JLabel simTitle = createLabel("The top teams from these simulations were:", textFont, null);
		if (reverse) simTitle.setText("The bottom teams from these simulations were:");
		simTitle.setBorder(new EmptyBorder(25, 0, 0, 0));
		
		JPanel simWinners = createPanel(new GridLayout(13, 1), 1);
		JPanel categoryTitle = createPanel(new GridLayout(1, 3), 0);
		JLabel column1Title = createLabel("Times Won:", textFont, null);
		if (statsInt == 1) column1Title.setText("Games Played:");
		else if (statsInt == 2) column1Title.setText("Goals Conceded:");
		else if (statsInt == 3) column1Title.setText("Won in ET:");
		else if (statsInt == 4) column1Title.setText("Won on Pens:");
		categoryTitle.add(column1Title);
		JLabel column2Title = createLabel("Games Won:", textFont, null);
		if (statsInt == 1) column2Title.setText("Games Drawn:");
		else if (statsInt == 2) column2Title.setText("Goal Difference:");
		else if (statsInt == 3) column2Title.setText("Drawn in ET:");
		else if (statsInt == 4) column2Title.setText("");
		categoryTitle.add(column2Title);
		JLabel column3Title = createLabel("Goals Scored:", textFont, null);
		if (statsInt == 1) column3Title.setText("Games Lost:");
		else if (statsInt == 2) column3Title.setText("Total Points:");
		else if (statsInt == 3) column3Title.setText("Lost in ET:");
		else if (statsInt == 4) column3Title.setText("Lost on Pens:");
		categoryTitle.add(column3Title);
		simWinners.add(categoryTitle);
		
		for (int i = 1; i <= 10; i++) {
			JPanel teamStats = createPanel(new GridLayout(1, 3), 0);
			JLabel simTeam = createLabel("", statFont, null);
			if (statsInt == 0)
				if (i <=  simTWonTeams.size()) simTeam.setText(i + ". " + simTWonTeams.get(i - 1).getName() + " | Times Won: " + simTWonTeams.get(i - 1).getTimesWon());
			if (statsInt == 1)
				if (i <=  simGPlayedTeams.size()) simTeam.setText(i + ". " + simGPlayedTeams.get(i - 1).getName() + " | Games Played: " + simGPlayedTeams.get(i - 1).getGPlayed() + " (avg. " + df.format(((float) simGPlayedTeams.get(i - 1).getGPlayed())/noOfSims) + " per run)");
			if (statsInt == 2)
				if (i <=  simGoalsConcededTeams.size()) simTeam.setText(i + ". " + simGoalsConcededTeams.get(i - 1).getName() + " | Goals Conceded: " + simGoalsConcededTeams.get(i - 1).getGoalsConceded() + " (avg. " + df.format(((float) simGoalsConcededTeams.get(i - 1).getGoalsConceded())/simGoalsConcededTeams.get(i - 1).getGPlayed()) + "pg)");
			if (statsInt == 3)
				if (i <=  simETWonTeams.size()) simTeam.setText(i + ". " + simETWonTeams.get(i - 1).getName() + " | Won in ET: " + simETWonTeams.get(i - 1).getETWon());
			if (statsInt == 4)
				if (i <=  simPenWonTeams.size()) simTeam.setText(i + ". " + simPenWonTeams.get(i - 1).getName() + " | Won on Pens: " + simPenWonTeams.get(i - 1).getPenWon());
			teamStats.add(simTeam);
			JLabel simTeam2 = createLabel("", statFont, null);
			if (statsInt == 0)
				if (i <=  simGWonTeams.size()) simTeam2.setText(i + ". " + simGWonTeams.get(i - 1).getName() + " | Games Won: " + simGWonTeams.get(i - 1).getGWon() + " (avg. " + df.format(((float) simGWonTeams.get(i - 1).getGWon())/noOfSims) + " per run)");
			if (statsInt == 1)
				if (i <=  simGDrawnTeams.size()) simTeam2.setText(i + ". " + simGDrawnTeams.get(i - 1).getName() + " | Games Drawn: " + simGDrawnTeams.get(i - 1).getGDrawn() + " (avg. " + df.format(((float) simGDrawnTeams.get(i - 1).getGDrawn())/noOfSims) + " per run)");
			if (statsInt == 2)
				if (i <=  simGoalDiffTeams.size()) simTeam2.setText(i + ". " + simGoalDiffTeams.get(i - 1).getName() + " | Goal Difference: " + simGoalDiffTeams.get(i - 1).getGoalDiff() + " (avg. " + df.format(((float) simGoalDiffTeams.get(i - 1).getGoalDiff())/noOfSims) + " per run)");
			if (statsInt == 3)
				if (i <=  simETDrawnTeams.size()) simTeam2.setText(i + ". " + simETDrawnTeams.get(i - 1).getName() + " | Drawn in ET: " + simETDrawnTeams.get(i - 1).getETDrawn());
			teamStats.add(simTeam2);
			JLabel simTeam3 = createLabel("", statFont, null);
			if (statsInt == 0)
				if (i <=  simGScoredTeams.size()) simTeam3.setText(i + ". " + simGScoredTeams.get(i - 1).getName() + " | Goals Scored: " + simGScoredTeams.get(i - 1).getGoalsScored() + " (avg. " + df.format(((float) simGScoredTeams.get(i - 1).getGoalsScored())/simGScoredTeams.get(i - 1).getGPlayed()) + "pg)");
			if (statsInt == 1)
				if (i <=  simGLostTeams.size()) simTeam3.setText(i + ". " + simGLostTeams.get(i - 1).getName() + " | Games Lost: " + simGLostTeams.get(i - 1).getGLost() + " (avg. " + df.format(((float) simGLostTeams.get(i - 1).getGLost())/noOfSims) + " per run)");
			if (statsInt == 2)
				if (i <=  simTotalPointsTeams.size()) simTeam3.setText(i + ". " + simTotalPointsTeams.get(i - 1).getName() + " | Total Points: " + simTotalPointsTeams.get(i - 1).getTotalPoints() + " (avg. " + df.format(((float) simTotalPointsTeams.get(i - 1).getTotalPoints())/noOfSims) + " per run)");
			if (statsInt == 3)
				if (i <=  simETLostTeams.size()) simTeam3.setText(i + ". " + simETLostTeams.get(i - 1).getName() + " | Lost in ET: " + simETLostTeams.get(i - 1).getETLost());
			if (statsInt == 4)
				if (i <=  simPenLostTeams.size()) simTeam3.setText(i + ". " + simPenLostTeams.get(i - 1).getName() + " | Lost on Pens: " + simPenLostTeams.get(i - 1).getPenLost());
			teamStats.add(simTeam3);
			
			simWinners.add(teamStats);
		}
		
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 3);
		JButton rateSimButton = createButton("Rate Sim", "rateSim", choiceHandler, buttonFont);
		buttonPanel.add(rateSimButton);
		
		if (statsInt != 4) {
			JButton moreStatsButton = createButton("More Stats", "gameStats", choiceHandler, buttonFont);
			if (statsInt == 1) moreStatsButton.setActionCommand("goalsStats");
			else if (statsInt == 2) moreStatsButton.setActionCommand("etStats");
			else if (statsInt == 3) moreStatsButton.setActionCommand("penStats");
			buttonPanel.add(moreStatsButton);
		}
		
		if (!reverse) {
			JButton bottomStatsButton = createButton("Bottom Side", "statsReverse", choiceHandler, buttonFont);
			if (statsInt == 1) bottomStatsButton.setActionCommand("gameStatsReverse");
			else if (statsInt == 2) bottomStatsButton.setActionCommand("goalsStatsReverse");
			else if (statsInt == 3) bottomStatsButton.setActionCommand("etStatsReverse");
			else if (statsInt == 4) bottomStatsButton.setActionCommand("penStatsReverse");
			buttonPanel.add(bottomStatsButton);
		} else {
			JButton topStatsButton = createButton("Top Side", "stats", choiceHandler, buttonFont);
			if (statsInt == 1) topStatsButton.setActionCommand("gameStats");
			else if (statsInt == 2) topStatsButton.setActionCommand("goalsStats");
			else if (statsInt == 3) topStatsButton.setActionCommand("etStats");
			else if (statsInt == 4) topStatsButton.setActionCommand("penStats");
			buttonPanel.add(topStatsButton);
		}
		
		if (statsInt != 0) {
			JButton backButton = createButton("Back", "stats", choiceHandler, buttonFont);
			if (statsInt == 2) backButton.setActionCommand("gameStats");
			else if (statsInt == 3) backButton.setActionCommand("goalsStats");
			else if (statsInt == 4) backButton.setActionCommand("etStats");
			buttonPanel.add(backButton);
		}
		
		buttonPanel = addEmptySpaces(buttonPanel, 3);
		con.add(simTitle, BorderLayout.NORTH);
		con.add(simWinners, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}

	// retrieve the given stats from the database
	public List<Team> retrieveData(int retrieveValue, boolean reverse, boolean statsPage) {
		List<Team> teams = new ArrayList<Team>();
		String query = "";
		String ASCorDESC = "DESC";
		String retrieveString = "";
		
		if (retrieveValue == 9) retrieveString = "ETWon";
		else if (retrieveValue == 10) retrieveString = "ETDrawn";
		else if (retrieveValue == 11) retrieveString = "ETLost";
		else if (retrieveValue == 12) retrieveString = "PenWon";
		else retrieveString = "PenLost";
		
		String greaterThanZero = "WHERE " + retrieveString + " > 0";
		
		if (reverse) {
			ASCorDESC = "ASC";
			greaterThanZero = "";
		}
		
		if (statsPage) greaterThanZero = "";
		
		if (retrieveValue == 0) query = "SELECT * FROM WCTeams WHERE TimesWon > 0 ORDER BY TimesWon " + ASCorDESC;
		else if (retrieveValue == 1) query = "SELECT * FROM WCTeams ORDER BY GamesPlayed " + ASCorDESC + ", GoalsScored " + ASCorDESC + ", GoalDifference " + ASCorDESC;
		else if (retrieveValue == 2) query = "SELECT * FROM WCTeams ORDER BY GamesWon " + ASCorDESC + ", GoalsScored " + ASCorDESC + ", GoalDifference " + ASCorDESC;
		else if (retrieveValue == 3) query = "SELECT * FROM WCTeams ORDER BY GamesDrawn " + ASCorDESC + ", GoalsScored " + ASCorDESC + ", GoalDifference " + ASCorDESC;
		else if (retrieveValue == 4) query = "SELECT * FROM WCTeams ORDER BY GamesLost " + ASCorDESC + ", GoalsScored " + ASCorDESC + ", GoalDifference " + ASCorDESC;
		else if (retrieveValue == 5) query = "SELECT * FROM WCTeams ORDER BY GoalsScored " + ASCorDESC + ", GoalsScored " + ASCorDESC + ", GoalDifference " + ASCorDESC;
		else if (retrieveValue == 6) query = "SELECT * FROM WCTeams ORDER BY GoalsConceded " + ASCorDESC + ", GoalsScored " + ASCorDESC + ", GoalDifference " + ASCorDESC;
		else if (retrieveValue == 7) query = "SELECT * FROM WCTeams ORDER BY GoalDifference " + ASCorDESC + ", GoalsScored " + ASCorDESC + ", GoalDifference " + ASCorDESC;
		else if (retrieveValue == 8) query = "SELECT * FROM WCTeams ORDER BY TotalPoints " + ASCorDESC + ", GoalsScored " + ASCorDESC + ", GoalDifference " + ASCorDESC;
		else if (retrieveValue == 9) query = "SELECT * FROM WCTeams " + greaterThanZero + " ORDER BY ETWon " + ASCorDESC + ", GoalsScored " + ASCorDESC + ", GoalDifference " + ASCorDESC;
		else if (retrieveValue == 10) query = "SELECT * FROM WCTeams " + greaterThanZero + " ORDER BY ETDrawn " + ASCorDESC + ", GoalsScored " + ASCorDESC + ", GoalDifference " + ASCorDESC;
		else if (retrieveValue == 11) query = "SELECT * FROM WCTeams " + greaterThanZero + " ORDER BY ETLost " + ASCorDESC + ", GoalsScored " + ASCorDESC + ", GoalDifference " + ASCorDESC;
		else if (retrieveValue == 12) query = "SELECT * FROM WCTeams " + greaterThanZero + " ORDER BY PenWon " + ASCorDESC + ", GoalsScored " + ASCorDESC + ", GoalDifference " + ASCorDESC;
		else if (retrieveValue == 13) query = "SELECT * FROM WCTeams " + greaterThanZero + " ORDER BY PenLost " + ASCorDESC + ", GoalsScored " + ASCorDESC + ", GoalDifference " + ASCorDESC;
		else query = "SELECT * FROM WCTeams";
		
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				Team t = new Team(rs.getString("Name"), rs.getString("Short"), rs.getInt("WorldRanking"), rs.getString("Group"), rs.getInt("GamesPlayed"), rs.getInt("GamesWon"), rs.getInt("GamesDrawn"), rs.getInt("GamesLost"), rs.getInt("GoalsScored"), rs.getInt("GoalsConceded"), rs.getInt("GoalDifference"), rs.getInt("TotalPoints"), rs.getInt("TimesWon"), rs.getInt("ETWon"), rs.getInt("ETDrawn"), rs.getInt("ETLost"), rs.getInt("PenWon"), rs.getInt("PenLost"), rs.getInt("TeamMorale"), rs.getDouble("AvgGS"), rs.getDouble("AvgGC"), rs.getInt("QMP"), rs.getInt("QGS"), rs.getInt("QGC"), rs.getDouble("avgCards"), rs.getDouble("xGf"), rs.getDouble("xGa"), rs.getInt("predRisk"));
				teams.add(t);
			}
			stmt.close();
		} catch (SQLException e) {
			errorScreen(e.getMessage());
		}
		return teams;
	}
	
	// get the user to rate the outcome of their simulation
	public void gettingUserRating() {
		resetWindow();
		
		JPanel simsPanel = createPanel(new GridLayout(4, 1), 1);
		JLabel simsLabel = createLabel("HOW WOULD YOU RATE THE SIMULATION?", textFont, null);
		simsLabel.setBorder(new EmptyBorder(25, 0, 0, 0));
		
		slider = new JSlider(0, 10, 5);
		slider.setForeground(Color.white);
		slider.setMajorTickSpacing(1);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.addChangeListener(sliderChanger);
		slider.setPreferredSize(new Dimension(750, 100));
		slider.setFont(statFont);
		
		getUserInput = new JTextField("5");
		getUserInput.setHorizontalAlignment(SwingConstants.CENTER);
		getUserInput.setEditable(false);
		getUserInput.setPreferredSize(new Dimension(200, 50));
		getUserInput.setFont(statFont);
		
		JPanel sliderWrapper = new JPanel(new FlowLayout());
		sliderWrapper.setBackground(new Color(35, 2, 19));
		sliderWrapper.add(slider);
		JPanel ratingWrapper = new JPanel(new FlowLayout());
		ratingWrapper.setBackground(new Color(35, 2, 19));
		ratingWrapper.add(getUserInput);
		
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 3);
		JButton resetButton = createButton("Reset", "resetSlider", choiceHandler, buttonFont);
		buttonPanel.add(resetButton);
		JButton backButton = createButton("Continue", "feedback", choiceHandler, buttonFont);
		buttonPanel.add(backButton);
		buttonPanel = addEmptySpaces(buttonPanel, 3);
		
		simsPanel.add(sliderWrapper);
		simsPanel.add(ratingWrapper);
		con.add(simsLabel, BorderLayout.NORTH);
		con.add(simsPanel, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// display the finishing screen
	public void finishSimulation() {
		resetWindow();
		
		JLabel feedbackLabel = createLabel("Thanks for your feedback!", textFont, null);
		feedbackLabel.setVisible(true);
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 2);
		JButton playAgainButton = createButton("Play Again", "", choiceHandler, buttonFont);
		if (mode.equals("sim")) playAgainButton.setActionCommand("simAgain");
		else playAgainButton.setActionCommand("playCMAgain");
		buttonPanel.add(playAgainButton);
		JButton mainMenuButton = createButton("Main Menu", "mainMenu", choiceHandler, buttonFont);
		buttonPanel.add(mainMenuButton);
		JButton exitButton = createButton("Exit Game", "exit", choiceHandler, buttonFont);
		buttonPanel.add(exitButton);
		buttonPanel = addEmptySpaces(buttonPanel, 2);
		buttonPanel.setVisible(true);
		
		con.add(feedbackLabel, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// ask the user if they want to play again with the same team
	public void playCMwithSameTeam() {
		resetWindow();
		
		JLabel sameTeamLabel = createLabel("Would you like to play again with the same team?", textFont, null);
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 2);
		JButton sameTeamButton = createButton("Same Team", "sameTeam", choiceHandler, buttonFont);
		buttonPanel.add(sameTeamButton);
		JButton diffTeamButton = createButton("Diff. Team", "diffTeam", choiceHandler, buttonFont);
		buttonPanel.add(diffTeamButton);
		JButton exitButton = createButton("Exit Game", "exit", choiceHandler, buttonFont);
		buttonPanel.add(exitButton);
		buttonPanel = addEmptySpaces(buttonPanel, 2);
		
		con.add(sameTeamLabel, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// tell the user that their progress was saved successfully
	public void saveGameMsg() {
		resetWindow();
		
		JLabel saveGameLabel = createLabel("Saved Game!", textFont, null);
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 3);
		JButton backButton = createButton("Back", "afterSave", choiceHandler, buttonFont);
		buttonPanel.add(backButton);
		buttonPanel = addEmptySpaces(buttonPanel, 3);
		
		con.add(saveGameLabel, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// ask the user which file they want to save their progress in
	public void chooseSaveFile() {
		resetWindow();
		
		JLabel saveGameLabel = createLabel("Which save file would you like to use?", textFont, null);
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 2);
		JButton file1Button = createButton("Save File 1", "saveFile1", choiceHandler, buttonFont);
		buttonPanel.add(file1Button);
		JButton file2Button = createButton("Save File 2", "saveFile2", choiceHandler, buttonFont);
		buttonPanel.add(file2Button);
		JButton file3Button = createButton("Save File 3", "saveFile3", choiceHandler, buttonFont);
		buttonPanel.add(file3Button);
		buttonPanel = addEmptySpaces(buttonPanel, 2);
		
		con.add(saveGameLabel, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// ask the user which saved file they want to load
	public void checkingSaveFiles() {
		resetWindow();
		
		JLabel saveGameLabel = createLabel("Which save file would you like to load?", textFont, null);
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 2);
		JButton file1Button = createButton("Save File 1", "loadFile1", choiceHandler, buttonFont);
		buttonPanel.add(file1Button);
		JButton file2Button = createButton("Save File 2", "loadFile2", choiceHandler, buttonFont);
		buttonPanel.add(file2Button);
		JButton file3Button = createButton("Save File 3", "loadFile3", choiceHandler, buttonFont);
		buttonPanel.add(file3Button);
		JButton backButton = createButton("Back", "mainMenu", choiceHandler, buttonFont);
		buttonPanel.add(backButton);
		buttonPanel = addEmptySpaces(buttonPanel, 2);
		
		con.add(saveGameLabel, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// display the error screen
	public void errorScreen(String errorMsg) {
		resetWindow();
		
		JLabel errorLabel = createLabel(errorMsg, textFont, null);
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 3);
		JButton exitButton = createButton("Exit", "exit", choiceHandler, buttonFont);
		buttonPanel.add(exitButton);
		buttonPanel = addEmptySpaces(buttonPanel, 3);
		
		con.add(errorLabel, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// update the given team's morale in the database
	public void updateTeamMorale(String teamName, int teamMorale) {
		String query = "UPDATE WCTeams SET TeamMorale = ? WHERE Name = ?";
		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, teamMorale);
			pstmt.setString(2, teamName);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			errorScreen(e.getMessage());
		}
	}
	
	// display the results of the group stage games, for the given group
	public void displayGroupGameResults(List<Team> group) {
		resetWindow();
		
		List<Match> matches = retrieveMatches(group.get(0).getGroup(), "", "");
		
		JLabel groupNameLabel = createLabel("Group " + group.get(0).getGroup() + " Results", titleFont, null);
		groupNameLabel.setBorder(new EmptyBorder(25, 0, 0, 0));
		JPanel groupResultsPanel = createPanel(new GridLayout(9, 1), 1);
		for (Match m : matches) {
			JPanel match = createPanel(new GridLayout(1, 5), 0);
			JLabel hTeam = createLabel(m.gethTeam(), textFont, null);
			match.add(hTeam);
			JLabel hTeamGoals = createLabel(Integer.toString(m.gethTeamGoals()), textFont, null);
			match.add(hTeamGoals);
			JLabel vs = createLabel(" - ", textFont, null);
			match.add(vs);
			JLabel aTeamGoals = createLabel(Integer.toString(m.getaTeamGoals()), textFont, null);
			match.add(aTeamGoals);
			JLabel aTeam = createLabel(m.getaTeam(), textFont, null);
			match.add(aTeam);
			
			if (m.gethTeam().equals(userTeam.getName())) {
				hTeam.setFont(userTeamFont.deriveFont(45f));
				hTeamGoals.setFont(userTeamFont.deriveFont(45f));
			} else if (m.getaTeam().equals(userTeam.getName())) {
				aTeam.setFont(userTeamFont.deriveFont(45f));
				aTeamGoals.setFont(userTeamFont.deriveFont(45f));
			}
			
			groupResultsPanel.add(match);
		}
		
		JPanel buttonPanel = createPanel(new GridLayout(1, 7), 2);
		
		if (groupCount < 7) {
			JButton nextGroupButton = createButton("Next Group", "nextGroup", choiceHandler, buttonFont);
			buttonPanel.add(nextGroupButton);
		} else {
			JButton nextStageButton = createButton("Next Stage", "checkProgressed", choiceHandler, buttonFont);
			buttonPanel.add(nextStageButton);
		}
		
		JButton groupTableButton = createButton("Group Table", "groupTable", choiceHandler, buttonFont);
		buttonPanel.add(groupTableButton);
		
		if (groupCount > 0) {
			JButton previousGroupButton = createButton("Previous Group", "previousGroup", choiceHandler, buttonFont);
			buttonPanel.add(previousGroupButton);
		}
		
		JButton exitGameButton = createButton("Exit Game", "exit", choiceHandler, buttonFont);
		buttonPanel.add(exitGameButton);
		buttonPanel = addEmptySpaces(buttonPanel, 2);
		
		con.add(groupNameLabel, BorderLayout.NORTH);
		con.add(groupResultsPanel, BorderLayout.CENTER);
		con.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	// retrieve the given match from the database
	public ArrayList<Match> retrieveMatches(String group, String team1, String team2) {
		ArrayList<Match> matches = new ArrayList<Match>();
		String query = "";
		int mID = -1;
		
		if (KOmatches == 0) mID = 49 + KOcount;
		else if (KOmatches == 1) mID = 57 + KOcount;
		else if (KOmatches == 2) mID = 61 + KOcount;
		
		if (!group.equals("KO")) {
			if (!team1.equals("") && !team2.equals("")) query = "SELECT * FROM WCMatches WHERE [Group] = '" + group + "' AND (hTeam = '" + team1 + "' OR hTeam = '" + team2 + "') AND (aTeam = '" + team1 + "' OR aTeam = '" + team2 + "')";
			else query = "SELECT * FROM WCMatches WHERE [Group] = '" + group + "'";
		} else query = "SELECT * FROM WCMatches WHERE [MatchID] = " + mID + "";
		
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				Match m = new Match(rs.getInt("MatchID"), rs.getString("hTeam"), rs.getInt("hTeamGoals"), rs.getInt("aTeamGoals"), rs.getString("aTeam"), rs.getString("Group"), rs.getInt("hTeamETGoals"), rs.getInt("aTeamETGoals"), rs.getInt("hTeamPens"), rs.getInt("aTeamPens"));
				matches.add(m);
			}
			stmt.close();
		} catch (SQLException e) {
			errorScreen(e.getMessage());
		}
		return matches;
	}
	
	// create a JLabel
	public JLabel createLabel(String labelText, Font f, Border b) {
		JLabel label = new JLabel(labelText);
		label.setBackground(new Color(35, 2, 19));
		label.setForeground(Color.white);
		label.setFont(f);
		label.setBorder(b);
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setVerticalAlignment(JLabel.CENTER);
		return label;
	}
	
	// create a JButton
	public JButton createButton(String buttonText, String actionCommand, ActionListener aL, Font f) {
		JButton button = new JButton(buttonText);
		button.setBackground(Color.white);
		button.setForeground(Color.black);
		button.setFont(f);
		button.setActionCommand(actionCommand);
		button.addActionListener(aL);
		button.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		        button.setFont(textFont);
		    }

		    public void mouseExited(java.awt.event.MouseEvent evt) {
		    	button.setFont(f);
		    }
		});
		return button;
	}
	
	// create a JPanel
	public JPanel createPanel(GridLayout gl, int emptySpaces) {
		JPanel panel;
		if (gl == null) panel = new JPanel();
		else panel = new JPanel(gl);
		panel.setBackground(new Color(35, 2, 19));
		panel = addEmptySpaces(panel, emptySpaces);
		return panel;
	}
	
	// add empty spaces into the JPanel
	public JPanel addEmptySpaces(JPanel jp, int emptySpaces) {
		for (int i = 0; i < emptySpaces; i++)
			jp.add(new JLabel());
		return jp;
	}
	
	// create a JLabel with a GIF
	public JLabel playGIF(String mediaURL) {
		try {
			Icon imgIcon = new ImageIcon(this.getClass().getResource("/" + mediaURL));
			JLabel gifLabel = new JLabel(imgIcon);
			return gifLabel;
		} catch (Exception e) {
			return null;
		}
	}
	
	// play the given sound
	@SuppressWarnings("static-access")
	public void playSound(String mediaURL) {
		try {
			if (mediaURL.equals("buttonClick.wav")) {
				buttonClip = AudioSystem.getClip();
				buttonClip.open(AudioSystem.getAudioInputStream(this.getClass().getResource("/" + mediaURL)));
				buttonClip.start();
			} else {
				clip = AudioSystem.getClip();
				clip.open(AudioSystem.getAudioInputStream(this.getClass().getResource("/" + mediaURL)));
				clip.start();
				if (mediaURL.equals("mainMusic.wav")) clip.loop(clip.LOOP_CONTINUOUSLY);
			}
	    }
	    catch (Exception exc) {
	        return;
	    }
	}
}