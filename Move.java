/*
 * Move.java
 *
 * Version:
 *    $Id$
 *
 * Revisions:
 *    &Log$
 *
 */

/** 
 * This class describes a valid or invalid move in the game of Othello
 * By default, all new Moves are not legal, have no points, and do not have a 
 * valid position on the board.
 *
 * @author     Francis Yuan
 *
 */

public class Move
{
	boolean legal = false;
	int points = 0;
	int x = -1;
	int y = -1;
}  