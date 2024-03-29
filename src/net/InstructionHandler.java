package net;

import cls.Aircraft;
import btc.Main;
import scn.Game;
import scn.Game.DifficultySetting;
import scn.MultiPlayerGame;

/**
 * Handles instructions.
 * <p>
 * Instructions should be of the form:
 * 'COMMAND':'PARAMETERS;
 * </p>
 * <p>
 * Multiple instructions can be handled by passing
 * in a string of the form:
 * 'Instruction1';'Instruction2'
 * When multiple instructions are passed, they will be
 * handled sequentially, in the order they appear in the
 * string.
 * </p>
 */
public abstract class InstructionHandler {
	
	/** The instruction list delimiter */
	public static final String LIST_DELIM = ";";
	
	/** The instruction delimiter */
	public static final String DELIM = ":";
	
	/** The messages to be delivered to the main thread */
	private static String messages = "";
	
	
	/**
	 * Handles instructions.
	 * <p>
	 * Takes a semicolon-delimited list of instructions and
	 * processes them sequentially.
	 * </p>
	 * @param instruction - the instruction(s) to handle
	 */
	public static void handleInstruction(String instruction) {
		if (instruction != null) {
			// Split the instruction string into individual instructions
			String[] instructionList = instruction.split(LIST_DELIM);

			// Check that there is at least one instruction
			if (instructionList != null) {
				if (instructionList.length > 0) {
					// Loop through the instructions, handling them
					// sequentially
					for (String instr : instructionList) {
						handleIndividualInstruction(instr);
					}
				}
			}
		}
	}
	
	/**
	 * Handles an instruction.
	 * <p>
	 * Breaks an instruction down into an instruction part
	 * and a parameter part, and passes these to the
	 * appropriate method (as specified in the instruction
	 * part.
	 * </p>
	 * @param instruction - the instruction to handle
	 */
	private static void handleIndividualInstruction(String instruction) {
		// Get the instruction
		String instr = instruction.split(DELIM)[0];
		
		// Return immediately if the instruction is invalid
		if (instr == null) return;
		
		// Check if the received data has parameters
		String parameters = null;
		if (instruction != null && instruction.contains(DELIM)) {
			parameters = instruction.substring(instruction.indexOf(DELIM) + 1);
		}
		
		// Switch to the appropriate method
		switch (instr) {
		case "SET_SEED":
			handleSetSeed(parameters);
			break;
		case "START_GAME":
			handleStartGame(parameters);
			break;
		case "GAME_OVER":
			handleGameOver(parameters);
			break;
		case "END_GAME":
			handleEndGame();
			break;
		}
	}
	
	
	/**
	 * Handles a SET_SEED instruction.
	 * <p>
	 * SET_SEED instructions set the random seed used by the game.
	 * </p>
	 * <p>
	 * This will cause random events to by synchronised across all
	 * players using the seed provided.
	 * </p>
	 * @param parameters - the parameters accompanying the instruction
	 */
	private static void handleSetSeed(String parameters) {
		// Get the player ID to set from the response
		int seedToSet = 0;
		try {
			seedToSet = Integer.parseInt(parameters);
		} catch (Exception e) {
			NetworkManager.print(e);
		}

		// Set the current player's random seed
		Main.setRandomSeed(seedToSet);

		NetworkManager.print("Using random seed: " + seedToSet);
	}
	
	/**
	 * Handles a START_GAME instruction.
	 * <p>
	 * START_GAME instructions cause a new instance of MultiPlayerGame
	 * to be created.
	 * </p>
	 * @param parameters - the parameters accompanying the instruction
	 */
	private static void handleStartGame(String parameters) {
		if (Thread.currentThread().getId() != NetworkManager.getNetworkThreadID()) {
			// Get the position to set from the response
			int playerPosition = -1;
			try {
				playerPosition = Integer.parseInt(parameters);
			} catch (Exception e) {
				NetworkManager.print(e);
			}

			// Start a new multiplayer game
			Main.setScene(MultiPlayerGame
					.createMultiPlayerGame(DifficultySetting.EASY,
							playerPosition));
		} else {
			// Obtain a lock on the message buffer
			synchronized (messages) {
				// Add a START_GAME instruction to the message buffer
				messages = "START_GAME:" + parameters;
			}
		}
	}
	
	/**
	 * Handles an GAME_OVER instruction.
	 * <p>
	 * GAME_OVER instructions cause the current game instance to end,
	 * directing the player to the GameOver scene.
	 * </p>
	 * @param parameters - the parameters accompanying the instruction
	 */
	private static void handleGameOver(String parameters) {
		// Get aircraft ID's from the parameters
		String[] params = parameters.split(DELIM);
		
		// Get the aircraft from their IDs
		Aircraft a1 = Game.getInstance().getAircraftFromName(params[0]);
		Aircraft a2 = Game.getInstance().getAircraftFromName(params[1]);
		
		if (Game.getInstance() != null) {
			// Obtain a lock on the game instance
			synchronized(Game.getInstance()) {
				((MultiPlayerGame) Game.getInstance())
						.setPassedCollidingAircraft(new Aircraft[] {a1, a2});
				((MultiPlayerGame) Game.getInstance()).setExitingToGameOver();
			}
		}
	}
	
	/**
	 * Handles an END_GAME instruction.
	 * <p>
	 * END_GAME instructions cause the current game instance to end.
	 * </p>
	 */
	private static void handleEndGame() {
		if (Game.getInstance() != null) {
			// Obtain a lock on the game instance
			synchronized(Game.getInstance()) {
				((MultiPlayerGame) Game.getInstance()).setExitingToLobby();
			}
		}
	}
	
	
	/**
	 * Gets any messages which need to be processed by the main thread.
	 * <p>
	 * This operation is <b>destructive</b>, i.e. the message buffer will
	 * be cleared after it has been read.
	 * </p>
	 * @return the contents of the message buffer
	 */
	public static String getMessages() {
		// Obtain a lock on the message buffer
		synchronized (messages) {
			String messageBuffer = messages;
			messages = "";
			return messageBuffer;
		}
	}
	
}
