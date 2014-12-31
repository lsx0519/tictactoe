/*
 * Copyright (C) 2013--2015 Richard Preen <rpreen@gmail.com>
 * An algorithm that plays tic tac toe (naughts and crosses) optimally
 * against a human using the negamax implementation of minimax. 
 * See: <https://en.wikipedia.org/wiki/Negamax>
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

public class NegaMax
{
	private final int NUM_ACTIONS = 9;
	private boolean gameEnded;

	public static void main(String[] args)
	{
		new NegaMax();
	}

	public NegaMax()
	{
		// initialise grid
		int[] curState = new int[NUM_ACTIONS];
		for(int i = 0; i < NUM_ACTIONS; i++)
			curState[i] = 0;
		// display game info
		System.out.printf("You are: %s\n", printSymbol(2));
		System.out.printf("Computer is: %s\n", printSymbol(1));
		System.out.printf("Moves:\n");
		printGridMoves();
		// print the current state of the board
		printGrid(curState);
		// play a single game
		int curPlayer = 2;
		Scanner scan = new Scanner(System.in);
		int move = 0;
		gameEnded = false;
		int movesRemaining = NUM_ACTIONS;
		do {
			// human
			if(curPlayer == 2) {
				System.out.println("enter move: ");
				move = scan.nextInt();
				curState[move] = 2;
			}
			// agent
			else {
				int AIMOVE = negaMax(1, curState)[1];
				if(AIMOVE > -1)
					curState[AIMOVE] = 1;
				else
					System.out.println("agent suggested invalid move: "+AIMOVE);
			}
			// is the game over?
			movesRemaining--;
			int reward = eval(curState);
			if(reward == -1 || reward == 1 || movesRemaining <= 0)
				gameEnded = true;
			if(curPlayer == 1) 
				curPlayer = 2;
			else 
				curPlayer = 1;
			printGrid(curState);
		}while(!gameEnded);
	}

	private int[] negaMax(int p, int[] state)
	{
		int move = -1;
		// valid moves from this board state
		List<Integer> validMoves = new ArrayList<Integer>();
		for(int i = 0; i < state.length; i++) {
			if(state[i] == 0)
				validMoves.add(i);
		}
		// game over
		int reward = eval(state);
		if(reward != 0 || validMoves.size() < 1)
			return (new int[] { reward, move });
		// game not over: check each sub-board
		int bestValue = 0;
		// computer
		if(p == 1) {
			bestValue = Integer.MIN_VALUE;
			for(int i = 0; i < validMoves.size(); i++) {
				int[] tmpState = new int[9];
				System.arraycopy(state, 0, tmpState, 0, state.length);
				tmpState[validMoves.get(i)] = p;

				int val = negaMax(2, tmpState)[0];
				if(val > bestValue) {
					bestValue = val;
					move = validMoves.get(i);
				}
			}
		}
		// human
		else
		{
			bestValue = Integer.MAX_VALUE;
			for(int i = 0; i < validMoves.size(); i++) {
				int[] tmpState = new int[9];
				System.arraycopy(state, 0, tmpState, 0, state.length);
				tmpState[validMoves.get(i)] = p;
				int val = negaMax(1, tmpState)[0];
				if(val < bestValue) {
					bestValue = val;
					move = validMoves.get(i);
				}
			}
		}
		return (new int[] { bestValue, move });
	}

	private void printGridMoves()
	{
		for(int i = 0; i < 9; i++) {
			System.out.printf("[%d]", i);
			if(i == 2 || i == 5 || i == 8)
				System.out.printf("\n");
		}
		System.out.println("");
	}

	private void printGrid(int[] state)
	{
		for(int i = 0; i < 9; i++) {
			System.out.printf("%s", printSymbol(state[i]));
			if(i == 2 || i == 5 || i == 8)
				System.out.printf("\n");
		}
		System.out.println("");
	}

	private String printSymbol(int s)
	{
		switch(s) {
			case 1: return "[X]";
			case 2: return "[O]";
			default: return "[ ]";
		}
	}

	private int eval(int[] state)
	{
		if(isWinner(state, 1))
			return 1;
		else if(isWinner(state, 2))
			return -1;
		else
			return 0;
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
				|| (state[2]==state[4] && state[4]==state[6] && state[2]==player))
			return true;
		else
			return false;
	}
}
