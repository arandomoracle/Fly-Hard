package cls;

import cls.Aircraft;
import scn.SinglePlayerGame;

public class Score {

	private int totalScore = 0;
	
	
		
	

	
	public static void update() {
		
		for (int i = 0; i < SinglePlayerGame.getAircraftList().size(); i++) {
			Aircraft plane = SinglePlayerGame.getAircraftList().get(i);
			if (plane.isFinished() == true){
				
			}
			
		}

	}

	}