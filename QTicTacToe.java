/*
 * Copyright (C) 2013--2015 Richard Preen <rpreen@gmail.com>
 * An algorithm that learns to play tic tac toe (naughts and crosses) by
 * playing against itself using Q-learning. 
 * See: <https://en.wikipedia.org/wiki/Q-learning>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import java.util.*;

public class QTicTacToe
{
	private static final int GAMES = 71000; // last 1000 are used for testing
	private static final double ALPHA = 0.1; // learning rate
	private static final double GAMMA = 0.9; // discount factor
	private static final int GRID_SIZE = 9;
	private boolean displayBoard = false;
	private boolean verbose = false;
	private boolean playOptimal = false;

	private Random rand;
	private Map<String, HashMap<Integer, Double>> qvalues;
	private boolean gameEnded;

	public static void main(String[] args)
	{
		new QTicTacToe();
	}

	public QTicTacToe()
	{
		// seed random number generator
		rand = new Random();
		// initialise empty qvalue table
		qvalues = new HashMap<String, HashMap<Integer, Double>>();
		// learn by playing games
		System.out.println("Learning... ");
		int gamesPlayed = 0, wins = 0, losses = 0, ties = 0;
		while(gamesPlayed < GAMES) {
			// last 1000 games will be optimal playing
			if(gamesPlayed == GAMES-1000) {
				System.out.println("Testing... ");
				wins = 0; losses = 0; ties = 0;
				playOptimal = true;
				verbose = false;//true;
				displayBoard = false;// true;
			}
			// initialise board
			int[] curState = new int[GRID_SIZE];
			for(int i = 0; i < GRID_SIZE; i++)
				curState[i] = 0;
			gameEnded = false;
			int movesRemaining = GRID_SIZE;
			// print the current state of the board
			if(displayBoard) {
				System.out.println("**************************************************************************************");
				printGrid(curState);
			}
			// set starting player
			int curPlayer = rand.nextInt(2)+1; // random
			// if(playOptimal)
			// curPlayer = 1; // player 1 always starts in the last 1000 games

			// play a single game
			do {
				// make stateKey, if key doesn't exist it generates a new one 
				// with valid actions initialised to random qvalues
				String stateKey = makeKey(curState, curPlayer); 
				// choose an action
				int action = chooseAction(stateKey, curPlayer, curState);
				if(verbose)
					System.out.println("Player = "+curPlayer+" action = "+action);
				// calculate next state
				int[] nextState = new int[curState.length];
				System.arraycopy(curState, 0, nextState, 0, curState.length);
				nextState[action] = curPlayer;
				movesRemaining--;
				// get immediate reward
				double reward = eval(nextState);
				// is the game over?
				if(reward == -1.0 || reward == 1.0 || movesRemaining <= 0) {
					if(verbose)
						System.out.println("game has ended");
					gameEnded = true;
					if(reward == 1.0)
						wins++;
					else if(reward == -1.0)
						losses++;
					else
						ties++;
				}
				// make nextKey
				int nextPlayer = 1;
				if(curPlayer == 1)
					nextPlayer = 2;
				String nextKey = makeKey(nextState, nextPlayer);
				// update q table
				updateQValue(curPlayer, stateKey, action, nextKey, reward);
				// set the next state as the current state -- executes action
				System.arraycopy(nextState, 0, curState, 0, nextState.length);
				// switch players
				if(curPlayer == 1)
					curPlayer = 2;
				else 
					curPlayer = 1;
				// print the current state of the board
				if(displayBoard)
					printGrid(curState);
			}while(!gameEnded);
			gamesPlayed++;
			// display wins/losses every 1000 games
			if(gamesPlayed % 1000 == 0) {
				if(playOptimal)
					System.out.printf("Wins: %d, Losses: %d, Ties: %d\n", wins, losses, ties);
				else
					System.out.printf("Played: %d, Wins: %d, Losses: %d, Ties: %d\n", gamesPlayed, wins, losses, ties);
			}
		}
	}

	private int chooseAction(String stateKey, int curPlayer, int[] curState)
	{
		// testing
		if(playOptimal) {
			if(curPlayer == 1)
				return getMaxQAction(stateKey);  // player 1 plays best policy
				//return randomMove(curState); // player 1 plays random
				//return getMinQAction(stateKey); // player 1 plays worst policy
			else
				//return getMinQAction(stateKey); // player 2 plays best policy
				//return randomMove(curState); // player 2 plays random
				return getMaxQAction(stateKey); // player 2 plays worst policy
		}
		// learning
		else {
			// 50% time choose greedy policy
			if(rand.nextDouble() < 0.5) {
				if(curPlayer == 1)
					return getMaxQAction(stateKey);
				else
					return getMinQAction(stateKey);
			}
			// 50% time choose random action
			else {
				return randomMove(curState);
			}
		}
	}

	private String makeKey(int[] state, int player)
	{
		String key = Arrays.toString(state) + Integer.toString(player);
		// if key not in the table then add key
		Map<Integer, Double> thisKeyEntry = qvalues.get(key);
		if(thisKeyEntry == null) {
			// create hashmap of all valid actions in this state and random initial qvalues
			HashMap<Integer, Double> vals = new HashMap<Integer, Double>();
			for(int i = 0; i < state.length; i++) {
				if(state[i] == 0)
					vals.put(i, (rand.nextDouble()*0.3)-0.15); // random [-0.15, 0.15] 
			}
			// add the hashmap to qvalues hashmap
			qvalues.put(key, vals);
		}
		return key;
	}

	private void updateQValue(int player, String stateKey, int action, String nextKey, double reward)
	{
		double expected = 0.0;
		if(gameEnded)
			expected = reward;
		else {
			// assumes that the opponent always makes the best move (similar to minimax)
			if(player == 1) 
				expected = reward + (GAMMA * minQ(nextKey));
			else
				expected = reward + (GAMMA * maxQ(nextKey));
		}
		double q = getQ(stateKey, action);
		if(verbose)
			System.out.printf("updating q: %.6f expected: %.6f immediate reward: %.6f", q, expected, reward);
		q += ALPHA * (expected - q);
		if(verbose)
			System.out.printf(" to: %.6f\n", q);
		setQ(stateKey, action, q);
	}

	private double maxQ(String stateKey)
	{
		int action = getMaxQAction(stateKey);
		return qvalues.get(stateKey).get(action);
	}

	private double minQ(String stateKey)
	{
		int action = getMinQAction(stateKey);
		return qvalues.get(stateKey).get(action);
	}

	private void setQ(String key, int action, double value)
	{
		// overwrite previous q value
		qvalues.get(key).put(action, value);
	}

	private double getQ(String key, int action)
	{
		return qvalues.get(key).get(action);
	}

	private int getMaxQAction(String stateKey)
	{
		double maxValue = -1.0;
		int bestAction = -1;
		Map<Integer, Double> vals = qvalues.get(stateKey);
		for(Map.Entry<Integer, Double> entry : vals.entrySet()) {
			if(entry.getValue() > maxValue) {
				maxValue = entry.getValue();
				bestAction = entry.getKey();
			}  
		}
		return bestAction;
	}

	private int getMinQAction(String stateKey)
	{
		double minValue = 1.0;
		int worstAction = -1;
		Map<Integer, Double> vals = qvalues.get(stateKey);
		for(Map.Entry<Integer, Double> entry : vals.entrySet()) {
			if(entry.getValue() < minValue) {
				minValue = entry.getValue();
				worstAction = entry.getKey();
			}  
		}
		return worstAction;
	}

	private int randomMove(int[] state)
	{
		ArrayList<Integer> moveList = new ArrayList<Integer>();
		for(int i = 0; i < state.length; i++)
			if(state[i] == 0)
				moveList.add(i);
		return moveList.get(rand.nextInt(moveList.size()));
	}

	private void printGrid(int[] state)
	{
		for(int i = 0; i < GRID_SIZE; i++) {
			System.out.printf("%s", getSymbol(state[i]));
			if(i == 2 || i == 5 || i == 8)
				System.out.printf("\n");
		}
		System.out.println("");
	}

	private String getSymbol(int s)
	{
		switch(s) {
			case 1: return "[X]";
			case 2: return "[O]";
			default: return "[ ]";
		}
	}

	private double eval(int[] state)
	{
		if(isWinner(state, 1)) {
			if(verbose)
				System.out.println("I WON");
			return 1.0;
		}
		else if(isWinner(state, 2)) {
			if(verbose)
				System.out.println("I LOST");
			return -1.0;
		}
		else
			return 0.0;
	}

	private boolean isWinner(int[] state, int player)
	{
		if((state[0]==state[1] && state[1]==state[2] && state[0]==player) // horizontal
			|| (state[3]==state[4] && state[4]==state[5] && state[3]==player) 
			|| (state[6]==state[7] && state[7]==state[8] && state[6]==player) 
			|| (state[0]==state[3] && state[3]==state[6] && state[0]==player) // vertical
			|| (state[1]==state[4] && state[4]==state[7] && state[1]==player)
			|| (state[2]==state[5] && state[5]==state[8] && state[2]==player)
			|| (state[0]==state[4] && state[4]==state[8] && state[0]==player) // diagonal
			|| (state[2]==state[4] && state[4]==state[6] && state[2]==player) )
			return true;
		else
			return false;
	}
}
