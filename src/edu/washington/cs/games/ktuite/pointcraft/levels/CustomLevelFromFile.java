package edu.washington.cs.games.ktuite.pointcraft.levels;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Scoring;

public class CustomLevelFromFile extends BaseLevel {

	protected String level_name = "Custom Level";
	
	public CustomLevelFromFile(Main main, String filename) {
		super(main);
		PointStore.load(filename);
		main.initData();
	}

	public void checkLevelState() {
		if (Scoring.score_delta > 0) {
			System.out.println("new points scored!");
			score += Scoring.score_delta;
			
			if (score == PointStore.num_points){
				System.out.println("100% of pts");
			}
			else if (score > PointStore.num_points * .5){
				System.out.println("50% of pts accounted for");
			}
		}

		Scoring.score_delta = 0;
	}

}