package indigo

enum class Messages(val msg: String) {
    Title("Indigo Card Game"),
    PlayPrompt("Play first?"),
    InitialCards("Initial cards on the table: %s"),
    Table("%s cards on the table, and the top card is %s"),
    TableEmpty("No cards on the table"),
    PlayerHand("Cards in hand: %s"),
    ComputerTurn("Computer plays %s"),
    CardChoice("Choose a card to play (1-%s):"),
    TableWon("%s wins cards"),
    GameOver("Game Over"),
    Points("Score: Player %s - Computer %s"),
    Cards("Cards: Player %s - Computer %s"),
    Generic("%s")
}

fun Messages.show(str1: String = Const.EMPTY, str2: String = Const.EMPTY) = println(this.msg.format(str1, str2))