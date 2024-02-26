//=============================================================================
//PROGRAMMER2: Alexis Rose Polowsky
// PANTHER ID2: 6371930
// CLASS: CAP5507 Introduction to Game Theory
//
// SEMESTER: Spring 2024
// CLASSTIME: 7pm
//
// Project: Coding Project
// DUE: Wednesday, Feb 22, 2024
//
// CERTIFICATION: I certify that this work is my own and that
// none of it is the work of any other person.
//=============================================================================
package app;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Controller
{
	private Random rng;

	private String mode;
	private int numPlayerOneStrats;
	private int numPlayerTwoStrats;

	private ArrayList<String> playerOneStrats;
	private ArrayList<String> playerTwoStrats;

	private ArrayList<Integer[]> playerOnePayoffs;
	private ArrayList<Integer[]> playerTwoPayoffs;

	private ArrayList<Integer> playerOneBestRow;
	private ArrayList<Integer> playerTwoBestCol;

	private ArrayList<Integer[]> nashEqLoc;

	private ArrayList<Float> playerOneBeliefs;
	private ArrayList<Float> playerTwoBeliefs;

	private ArrayList<Float> playerOnePayoffsOpponetMixing;
	private ArrayList<Float> playerTwoPayoffsOpponetMixing;

	private float playerOnePayoffActualMixing;
	private float playerTwoPayoffActualMixing;

	private int playerOneOpponetMixingBestRow;
	private int playerTwoOpponetMixingBestCol;

	private float playerOneIndiffProb;
	private float playerTwoIndiffProb;

	private String payoffFormatString;
	private String nfRowFormatString;
	private String nfHeadFormatString;
	private String hlineString;

	private String expectedPlayerOneFormatString;
	private String expectedPlayerTwoFormatString;

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		Controller controller = new Controller();
		if (controller.getMode().equals("R"))
		{
			controller.generateRandomPayoffs();
		}
		else if (controller.getMode().equals("M"))
		{
			controller.askForPayoffs();
		}

		controller.printStrategiesAndPayoffs();

		// handle special case of 2x2 normal form matrix
		if ((controller.getNumPlayerOneStrats() == 2) &&
				(controller.getNumPlayerTwoStrats() == 2))
		{
			if (!controller.nashEqExists())
			{
				// find indifferent mix probabilities
			}
			System.out.println("==================================================");
			System.out.println("Nash Pure Equilibrium Locations");
			System.out.println("==================================================");
			controller.findBestLocations();
			ArrayList<ArrayList<String>> normalForm =
													controller.createBestLocNormalForm();
			controller.printNormalForm(normalForm);
			controller.printPureNashEq();
			return;  // exit
		}

		System.out.println("==================================================");
		System.out.println("Display Normal Form");
		System.out.println("==================================================");
		ArrayList<ArrayList<String>> normalForm = controller.createNormalForm();
		controller.printNormalForm(normalForm);

		System.out.println();

		System.out.println("==================================================");
		System.out.println("Nash Pure Equilibrium Locations");
		System.out.println("==================================================");
		controller.findBestLocations();
		normalForm = controller.createBestLocNormalForm();
		controller.printNormalForm(normalForm);
		controller.printPureNashEq();

		controller.generateRandomBeliefs();
		controller.computeOpponentMixingPayoffs();
		controller.printPayoffsAndResponses();

		controller.computeActualMixingPayoffs();
		controller.printActualMixingPayoffs();
	}

	public Controller()
	{
		this(10);
	}

	/**
	 * Constructor
	 *
	 * @param rngSeed The seed number to be used for random mode
	 */
	public Controller(long rngSeed)
	{
		expectedPlayerTwoFormatString = "(";
		expectedPlayerOneFormatString = "(";
		hlineString = "   ";
		nfHeadFormatString = "";
		nfRowFormatString = "|";
		payoffFormatString = "";
		playerTwoOpponetMixingBestCol = -1;
		playerOneOpponetMixingBestRow = -1;
		playerTwoPayoffsOpponetMixing = new ArrayList<>();
		playerOnePayoffsOpponetMixing = new ArrayList<>();
		playerTwoBeliefs = new ArrayList<>();
		playerOneBeliefs = new ArrayList<>();
		nashEqLoc = new ArrayList<>();
		playerTwoBestCol = new ArrayList<>();
		playerOneBestRow = new ArrayList<>();
		playerTwoPayoffs = new ArrayList<>();
		playerOnePayoffs = new ArrayList<>();
		playerTwoStrats = new ArrayList<>();
		playerOneStrats = new ArrayList<>();
		rng = new Random(rngSeed);

		// Read configuration
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter (R)andom or (M)anual playoffs entries:");
		mode = scanner.nextLine();

		System.out.print("Enter number of rows: ");
		numPlayerOneStrats = scanner.nextInt();
		for(int i = 0; i < numPlayerOneStrats; i++)
		{
			playerOneStrats.add(String.format("A%d", i+1));

			expectedPlayerOneFormatString += "%.2f";
			if(i != numPlayerOneStrats - 1)
			{
				expectedPlayerOneFormatString += ",";
			}
		}
		expectedPlayerOneFormatString += ")";

		System.out.print("Enter number of columns: ");
		numPlayerTwoStrats = scanner.nextInt();
		for(int i = 0; i < numPlayerTwoStrats; i++)
		{
			playerTwoStrats.add(String.format("B%d", i+1));
			payoffFormatString += "%4d";
			if(i != numPlayerTwoStrats - 1)
			{
				payoffFormatString += ",";
			}

			expectedPlayerTwoFormatString += "%.2f";
			if(i != numPlayerTwoStrats - 1)
			{
				expectedPlayerTwoFormatString += ",";
			}

			nfRowFormatString += "%12s|";
			nfHeadFormatString += "%13s";
			hlineString += "-------------";
		}
		expectedPlayerTwoFormatString += ")";

		payoffFormatString += "%n";
		nfRowFormatString += "%n";
		nfHeadFormatString += "%n";
		hlineString += "-";
	}

	public void generateRandomPayoffs()
	{
		// create payoffs for first player
		int[] randomPayoffs1  = rng.ints(
			getNumPlayerOneStrats()*getNumPlayerTwoStrats(),-99,99
		).toArray();

		for (int i = 0; i < getNumPlayerOneStrats(); i++)
		{
			Integer[] temp = new Integer[getNumPlayerTwoStrats()];
			for (int j = 0; j < getNumPlayerTwoStrats(); j++)
			{
				temp[j] = randomPayoffs1[i*getNumPlayerOneStrats() + j];
			}
			playerOnePayoffs.add(temp);
		}

		// create payoffs for second player
		int[] randomPayoffs2  = rng.ints(
			getNumPlayerOneStrats()*getNumPlayerTwoStrats(),-99,99
		).toArray();
		for (int i = 0; i < getNumPlayerOneStrats(); i++)
		{
			Integer[] temp = new Integer[getNumPlayerTwoStrats()];
			for (int j = 0; j < getNumPlayerTwoStrats(); j++)
			{
				temp[j] = randomPayoffs2[i*getNumPlayerOneStrats() + j];
			}
			playerTwoPayoffs.add(temp);
		}
	}

	public void printStrategiesAndPayoffs()
	{
		System.out.println("\n---------------------------------------------");
		System.out.println("Player: Player1's Strategies");
		System.out.println("---------------------------------------------");
		System.out.println(playerOneStrats.toString());

		System.out.println("\n---------------------------------------------");
		System.out.println("Player: Player1's Payoffs");
		System.out.println("---------------------------------------------");
		for (int i = 0; i < getNumPlayerOneStrats(); i++)
		{
			Integer[] payoffs = playerOnePayoffs.get(i);
			System.out.format(payoffFormatString, (Object[]) payoffs);
		}

		System.out.println("\n---------------------------------------------");
		System.out.println("Player: Player2's Strategies");
		System.out.println("---------------------------------------------");
		System.out.println(playerTwoStrats.toString());

		System.out.println("\n---------------------------------------------");
		System.out.println("Player: Player2's Payoffs");
		System.out.println("---------------------------------------------");
		for (int i = 0; i < getNumPlayerOneStrats(); i++)
		{
			Integer[] payoffs = playerTwoPayoffs.get(i);
			System.out.format(payoffFormatString, (Object[]) payoffs);
		}
		System.out.println();
	}

	public ArrayList<ArrayList<String>> createNormalForm()
	{
		ArrayList<ArrayList<String>> normalForm = new ArrayList<>();
		for (int row = 0; row < getNumPlayerOneStrats(); row++)
		{
			ArrayList<String> tempArrayList = new ArrayList<>();
			for (int col = 0; col < getNumPlayerTwoStrats(); col++)
			{
				String temp = String.format(
					"(%d,%d)", playerOnePayoffs.get(row)[col],
					playerTwoPayoffs.get(row)[col]
				);
				tempArrayList.add(temp);
			}
			normalForm.add(tempArrayList);
		}
		return normalForm;
	}

	public void printNormalForm(ArrayList<ArrayList<String>> normalForm)
	{
		System.out.format("%-3s", "");
		System.out.format(nfHeadFormatString, playerTwoStrats.toArray());
		System.out.print(hlineString);
		for (int i = 0; i < getNumPlayerOneStrats(); i++)
		{
			System.out.format("\n%-3s", playerOneStrats.get(i));
			System.out.format(nfRowFormatString, normalForm.get(i).toArray());
			System.out.print(hlineString);
		}
		System.out.println();
	}

	public void findBestLocations()
	{
		// for player one
		for (int col = 0; col < getNumPlayerTwoStrats(); col++)
		{
			int bestRow = -1;
			int bestPayoff = -100;
			for (int row = 0; row < getNumPlayerOneStrats(); row++)
			{
				int currentPayoff = playerOnePayoffs.get(row)[col];
				if (bestPayoff < currentPayoff)
				{
					bestPayoff = currentPayoff;
					bestRow = row;
				}
			}
			playerOneBestRow.add(bestRow);
		}

		// for player two
		for (int row = 0; row < getNumPlayerOneStrats(); row++)
		{
			int bestCol = -1;
			int bestPayoff = -100;
			for (int col = 0; col < getNumPlayerTwoStrats(); col++)
			{
				int currentPayoff = playerTwoPayoffs.get(row)[col];
				if (bestPayoff < currentPayoff)
				{
					bestPayoff = currentPayoff;
					bestCol = col;
				}
			}
			playerTwoBestCol.add(bestCol);

			int p1BestRow = playerOneBestRow.get(bestCol);
			if (row == p1BestRow)
			{
				Integer[] temp = {row, bestCol};
				nashEqLoc.add(temp);
			}
		}
	}

	public ArrayList<ArrayList<String>> createBestLocNormalForm()
	{
		ArrayList<ArrayList<String>> normalForm = new ArrayList<>();
		for (int row = 0; row < getNumPlayerOneStrats(); row++)
		{
			ArrayList<String> tempArrayList = new ArrayList<>();
			String temp;

			int p2BestCol = playerTwoBestCol.get(row);
			for (int col = 0; col < getNumPlayerTwoStrats(); col++)
			{
				int p1BestRow = playerOneBestRow.get(col);
				if ((p1BestRow == row) && (p2BestCol == col))
				{
					temp = "(H,H)";
				}
				else if ((p1BestRow == row) && (p2BestCol != col))
				{
					temp = String.format(
					"(H,%d)", playerTwoPayoffs.get(row)[col]
					);
				}
				else if ((p1BestRow != row) && (p2BestCol == col))
				{
					temp = String.format(
					"(%d,H)", playerOnePayoffs.get(row)[col]
					);
				}
				else
				{
					temp = String.format(
						"(%d,%d)", playerOnePayoffs.get(row)[col],
						playerTwoPayoffs.get(row)[col]
					);
				}
				tempArrayList.add(temp);
			}
			normalForm.add(tempArrayList);
		}
		return normalForm;
	}

	public void printPureNashEq()
	{
		if (nashEqLoc.isEmpty())
		{
			System.out.println("No Nash Pure Equilibrium");
		}
		else
		{
			System.out.print("Nash Pure Equilibrium(s): ");
			for(Integer[] eqLoc: nashEqLoc)
			{
				int eqRow = eqLoc[0];
				int eqCol = eqLoc[1];
				System.out.format(
					"(%s,%s)    ", playerOneStrats.get(eqRow),
					playerTwoStrats.get(eqCol));
			}
			System.out.println();
		}
		System.out.println();
	}

	public void generateRandomBeliefs()
	{
		// for player one
		float beliefSum = 0;
		for (int i = 0; i < getNumPlayerOneStrats(); i++)
		{
			float nextBelief = rng.nextFloat();
			playerOneBeliefs.add(nextBelief);
			beliefSum += nextBelief;
		}

		for (int i = 0; i < getNumPlayerOneStrats(); i++)
		{
			playerOneBeliefs.set(i, playerOneBeliefs.get(i)/beliefSum);
		}

		// for player two
		beliefSum = 0;
		for (int i = 0; i < getNumPlayerTwoStrats(); i++)
		{
			float nextBelief = rng.nextFloat();
			playerTwoBeliefs.add(nextBelief);
			beliefSum += nextBelief;
		}

		for (int i = 0; i < getNumPlayerTwoStrats(); i++)
		{
			playerTwoBeliefs.set(i, playerTwoBeliefs.get(i)/beliefSum);
		}
	}

	public void computeOpponentMixingPayoffs()
	{
		// for player one
		float bestPayoff = -100;
		for (int row = 0; row < getNumPlayerOneStrats(); row++)
		{
			float expectedPayoff = 0;
			for(int col = 0; col < getNumPlayerTwoStrats(); col++)
			{
				int payoff = playerOnePayoffs.get(row)[col];
				float belief = playerTwoBeliefs.get(col);
				expectedPayoff += belief * payoff;
			}
			playerOnePayoffsOpponetMixing.add(expectedPayoff);
			if (expectedPayoff > bestPayoff)
			{
				playerOneOpponetMixingBestRow = row;
				bestPayoff = expectedPayoff;
			}
		}

		// for player one
		bestPayoff = -100;
		for (int col = 0; col < getNumPlayerTwoStrats(); col++)
		{
			float expectedPayoff = 0;
			for(int row = 0; row < getNumPlayerOneStrats(); row++)
			{
				int payoff = playerTwoPayoffs.get(row)[col];
				float belief = playerOneBeliefs.get(row);
				expectedPayoff += belief * payoff;
			}
			playerTwoPayoffsOpponetMixing.add(expectedPayoff);
			if (expectedPayoff > bestPayoff)
			{
				playerTwoOpponetMixingBestCol = col;
				bestPayoff = expectedPayoff;
			}
		}
	}

	public void printPayoffsAndResponses()
	{
		// for player one
		System.out.println("---------------------------------------------");
		System.out.println("Player1 Expected Payoffs with Player 2 Mixing");
		System.out.println("---------------------------------------------");
		for (int i = 0; i < getNumPlayerOneStrats(); i++)
		{
			System.out.format("U(%s,", playerOneStrats.get(i));
			System.out.format(
				expectedPlayerTwoFormatString, playerTwoBeliefs.toArray());
			System.out.format(") = %.2f%n", playerOnePayoffsOpponetMixing.get(i));
		}

		System.out.println("\n---------------------------------------------");
		System.out.println("Player 1 Best Response with Player 2 Mixing");
		System.out.println("---------------------------------------------");
		System.out.print("BR");
		System.out.format(
				expectedPlayerTwoFormatString, playerTwoBeliefs.toArray());
		System.out.format(
			" = {%s}%n", playerOneStrats.get(playerOneOpponetMixingBestRow)
		);

		// for player two
		System.out.println("\n---------------------------------------------");
		System.out.println("Player2 Expected Payoffs with Player 1 Mixing");
		System.out.println("---------------------------------------------");
		for (int i = 0; i < getNumPlayerTwoStrats(); i++)
		{
			System.out.format("U(%s,", playerTwoStrats.get(i));
			System.out.format(
				expectedPlayerOneFormatString, playerOneBeliefs.toArray());
			System.out.format(") = %.2f%n", playerTwoPayoffsOpponetMixing.get(i));
		}

		System.out.println("\n---------------------------------------------");
		System.out.println("Player 2 Best Response with Player 1 Mixing");
		System.out.println("---------------------------------------------");
		System.out.print("BR");
		System.out.format(
				expectedPlayerOneFormatString, playerOneBeliefs.toArray());
		System.out.format(
			" = {%s}%n", playerTwoStrats.get(playerTwoOpponetMixingBestCol)
		);
	}

	public void computeActualMixingPayoffs()
	{
		// for player one
		playerOnePayoffActualMixing = 0;
		for (int i = 0; i < getNumPlayerOneStrats(); i++)
		{
			playerOnePayoffActualMixing +=
				playerOneBeliefs.get(i) * playerOnePayoffsOpponetMixing.get(i);
		}

		// for player two
		playerTwoPayoffActualMixing = 0;
		for (int i = 0; i < getNumPlayerTwoStrats(); i++)
		{
			playerTwoPayoffActualMixing +=
				playerTwoBeliefs.get(i) * playerTwoPayoffsOpponetMixing.get(i);
		}
	}

	public void printActualMixingPayoffs()
	{
		System.out.println("\n---------------------------------------------");
		System.out.println("Player 1 & 2 Expected Payoffs with both Players Mixing");
		System.out.println("---------------------------------------------");

		// for player one
		System.out.print("Player1 -> U(");
		System.out.format(
			expectedPlayerOneFormatString, playerOneBeliefs.toArray());
		System.out.print(", ");
		System.out.format(
			expectedPlayerTwoFormatString, playerTwoBeliefs.toArray());
		System.out.format(") = %.2f%n", playerOnePayoffActualMixing);

		// for player two
		System.out.print("Player2 -> U(");
		System.out.format(
			expectedPlayerOneFormatString, playerOneBeliefs.toArray());
		System.out.print(", ");
		System.out.format(
			expectedPlayerTwoFormatString, playerTwoBeliefs.toArray());
		System.out.format(") = %.2f%n", playerTwoPayoffActualMixing);
	}

	public void computeIndifferenceProbabilities()
	{
		// make player one indifferent
		playerTwoIndiffProb = (
			(playerOnePayoffs.get(1)[1] - playerOnePayoffs.get(0)[1]) /
			(
				(playerOnePayoffs.get(1)[1] - playerOnePayoffs.get(0)[1]) +
				(playerOnePayoffs.get(0)[0] - playerOnePayoffs.get(1)[0])
			)
		);

		// make player two indifferent
		playerOneIndiffProb = (
			(playerTwoPayoffs.get(1)[1] - playerOnePayoffs.get(1)[0]) /
			(
				(playerOnePayoffs.get(1)[1] - playerOnePayoffs.get(1)[0]) +
				(playerOnePayoffs.get(0)[0] - playerOnePayoffs.get(0)[1])
			)
		);
	}

	public void printIndiffProbs()
	{
		System.out.println("--------------------------------------------------");
		System.out.println("Player 1 & 2 Indifferent Mix Probabilities");
		System.out.println("--------------------------------------------------");
		System.out.format(
			"Player 1 probability of strategies (A1) = %.2f%n",
			playerOneIndiffProb
		);
		System.out.format(
			"Player 1 probability of strategies (A2) = %.2f%n",
			1 - playerOneIndiffProb
		);
		System.out.format(
			"Player 2 probability of strategies (B1) = %.2f%n",
			playerTwoIndiffProb
		);
		System.out.format(
			"Player 2 probability of strategies (A2) = %.2f%n",
			1 - playerTwoIndiffProb
		);
	}

	public void askForPayoffs()
	{
		Scanner scanner = new Scanner(System.in);

		System.out.println("Manual Entries");
		for (int i = 0; i < getNumPlayerOneStrats(); i++)
		{
			Integer[] temp1 = new Integer[getNumPlayerTwoStrats()];
			Integer[] temp2 = new Integer[getNumPlayerTwoStrats()];
			for (int j = 0; j < getNumPlayerTwoStrats(); j++)
			{
				System.out.format(
					"Enter payoff for ( %s, %s ) = ",
					playerOneStrats.get(i),
					playerTwoStrats.get(j)
				);
				String inputStr = scanner.nextLine();
				String[] inputPayoffs = inputStr.split(",");
				temp1[j] = Integer.valueOf(inputPayoffs[0]);
				temp2[j] = Integer.valueOf(inputPayoffs[1]);
			}
			playerOnePayoffs.add(temp1);
			playerTwoPayoffs.add(temp2);
			System.out.println("--------------------------------------------------");
		}
	}

	/**
	 * @return the numPlayerOneStrats
	 */
	public int getNumPlayerOneStrats()
	{
		return numPlayerOneStrats;
	}

	/**
	 * @param numPlayerOneStrats the numPlayerOneStrats to set
	 */
	public void setNumPlayerOneStrats(int numPlayerOneStrats)
	{
		this.numPlayerOneStrats = numPlayerOneStrats;
	}

	/**
	 * @return the numPlayerTwoStrats
	 */
	public int getNumPlayerTwoStrats()
	{
		return numPlayerTwoStrats;
	}

	/**
	 * @param numPlayerTwoStrats the numPlayerTwoStrats to set
	 */
	public void setNumPlayerTwoStrats(int numPlayerTwoStrats)
	{
		this.numPlayerTwoStrats = numPlayerTwoStrats;
	}

	/**
	 * @return the mode
	 */
	public String getMode()
	{
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(String mode)
	{
		this.mode = mode;
	}

	public boolean nashEqExists()
	{
		return !nashEqLoc.isEmpty();
}
}
