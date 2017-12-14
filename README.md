Reversi (also known as Othello)
=======
This is the public release of a Reversi-playing bot. It was initially forked from https://github.com/haly/Othello and was developed by Tatsuhiro Koshi and myself for the CS4701 AI practicum class at Cornell. 

The bot can utilize a variety of different methods to determine its moves. Its primary method is Monte Carlo search, of which there is a basic and a merging variant. Other methods include a minimax search, a simple greedy decision algorithm, and a random-play algorithm. It can simulate human vs AI play as well as AI vs AI play both with and without a GUI. Logging information such as the bot's predicted winning percentage and total wins for a series of games are available.

You can modify the AIType argument in Othello.java's constructor to change the bot's method it will use to determine its moves for both the white and black players.

Run the program as a Java Application with one of the following program arguments:<br> 
* X : GUI with bot vs bot and delay of X milliseconds
*  -X : No GUI - run program in the background until X games have been played
*  0: GUI with human vs bot
