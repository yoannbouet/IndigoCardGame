package indigo

import kotlin.random.Random

data class Card(val rank: String, val suit: String) {
    override fun toString() = rank + suit
    fun points(): Int = if (this.rank in setOf("A", "10", "J", "Q", "K")) 1 else 0
}

data class Score(var cards: Int = 0, var points: Int = 0) {
    fun count(card: Card) {
        cards++
        points += card.points()
    }
    companion object {
        var player = Score()
        var computer = Score()
    }
}

class IndigoCardGame {
    private val deck = mutableSetOf<Card>()
    private val table = mutableSetOf<Card>()
    private val playerCards = mutableSetOf<Card>()
    private val computerCards = mutableSetOf<Card>()
    private val suits = setOf("♠", "♥", "♦", "♣")
    private val ranks = setOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    private var currentPlayer = Const.PLAYER
    private var firstPlayer = Const.PLAYER
    private lateinit var lastWonTable: String

    init {
        reset()
        Messages.Title.show()
        repeat(Const.TABLE_START) { deck.last().let {
            table.add(it)
            deck.remove(it)
        } }

        start@ while (true) {
            Messages.PlayPrompt.show()
            val input = readln()
            if (Regex("(${Const.YES})|(${Const.NO})", RegexOption.IGNORE_CASE).matches(input)) {
                Messages.InitialCards.show(table.joinToString(Const.BLANK))
                currentPlayer = if (input == Const.YES) Const.PLAYER else Const.COMPUTER
                firstPlayer = if (input == Const.YES) Const.PLAYER else Const.COMPUTER
                gameOngoing()
            } else continue@start
            break@start
        }

        Messages.GameOver.show()
    }

    private fun gameOngoing() {
        if (playerCards.isEmpty() && deck.size >= Const.CARDS_DEAL) dealCards(playerCards)
        if (computerCards.isEmpty() && deck.size >= Const.CARDS_DEAL) dealCards(computerCards)

        if (table.isEmpty()) Messages.TableEmpty.show()
        else Messages.Table.show(table.size.toString(), table.last().toString())
        if (playerCards.isEmpty() && computerCards.isEmpty() && deck.isEmpty()) {
            if (lastWonTable.isEmpty()) {
                if (firstPlayer == Const.PLAYER) {
                    table.forEach { Score.player.count(it) }
                } else {
                    table.forEach { Score.computer.count(it) }
                }
            } else {
                if (lastWonTable == Const.PLAYER) {
                    table.forEach { Score.player.count(it) }
                } else {
                    table.forEach { Score.computer.count(it) }
                }
            }
            if (Score.player.cards == Score.computer.cards) {
                if (firstPlayer == Const.PLAYER) {
                    Score.player.points += 3
                } else {
                    Score.computer.points += 3
                }
            } else if (Score.player.cards > Score.computer.cards) {
                Score.player.points += 3
            } else {
                Score.computer.points += 3
            }
            scoreDisplay()
            return
        }

        if (currentPlayer == Const.PLAYER) {
            Messages.PlayerHand.show(playerCards.mapIndexed { i, card ->
                "${i + 1})${card}" }.joinToString(Const.BLANK))
            choice@ while (true) {
                Messages.CardChoice.show(playerCards.size.toString())
                val card = readln().also { if (it == Const.EXIT) return }
                if (Regex("[1-${playerCards.size}]").matches(card)) {
                    playCard(playerCards.elementAt(card.toInt() - 1), playerCards, topCard())
                    break@choice
                } else continue@choice
            }
        } else {
            Messages.Generic.show(computerCards.joinToString(Const.BLANK))
            computerChoice().let {
                Messages.ComputerTurn.show(it.toString())
                playCard(it, computerCards, topCard())
            }
        }
        currentPlayer = if (currentPlayer == Const.PLAYER) Const.COMPUTER else Const.PLAYER
        gameOngoing()
    }

    private fun computerChoice() : Card {
        val candidates = mutableListOf<Card>()
        val suits = computerCards.groupBy { it.suit }.filter { it.value.size >= 2 }
        val ranks = computerCards.groupBy { it.rank }.filter { it.value.size >= 2 }
        val random = { until: Int -> Random.nextInt(until) }
        computerCards.forEach {
            if (it.suit == topCard()?.suit || it.rank == topCard()?.rank) candidates.add(it)
        }

        val cdSuits = candidates.groupBy { it.suit }.filter { it.value.size >= 2 }
        val cdRanks = candidates.groupBy { it.rank }.filter { it.value.size >= 2 }

        if (candidates.size == 1) return candidates.first()
        if (table.isEmpty()) {
            return if (suits.values.isNotEmpty()) {
                random(suits.values.size)
                    .let { suits.values.elementAt(it).elementAt(random(suits.values.elementAt(it).size)) }
            } else if (ranks.values.isNotEmpty()) {
                random(ranks.values.size).let {
                    ranks.values.elementAt(it).elementAt(random(ranks.values.elementAt(it).size))
                }
            } else {
                computerCards.elementAt(random(computerCards.size))
            }
        } else if (candidates.isEmpty()) {
            return if (suits.values.isNotEmpty()) {
                random(suits.values.size).let {
                    suits.values.elementAt(it).elementAt(random(suits.values.elementAt(it).size))
                }
            } else if (ranks.values.isNotEmpty()) {
                random(ranks.values.size).let {
                    ranks.values.elementAt(it).elementAt(random(ranks.values.elementAt(it).size))
                }
            } else {
                computerCards.elementAt(random(computerCards.size))
            }
            // TWO+ CANDIDATES
        } else {
            return if (cdSuits.isNotEmpty() && ((cdSuits.values.maxByOrNull { it.size }?.size
                    ?: 0) >= (cdRanks.values.maxByOrNull { it.size }?.size ?: 0))) {
                random(cdSuits.values.size).let {
                    cdSuits.values.elementAt(it).elementAt(random(cdSuits.values.elementAt(it).size)) }
            } else if (cdRanks.isNotEmpty()) {
                random(cdRanks.values.size).let {
                    cdRanks.values.elementAt(it).elementAt(random(cdRanks.values.elementAt(it).size)) }
            } else {
                candidates.elementAt(random(candidates.size))
            }
        }
    }

    private fun topCard(): Card? {
        if (table.isEmpty()) return null
        return table.last()
    }

    private fun playCard(card: Card, hand: MutableSet<Card>, top: Card?) {
        table.add(card)
        hand.remove(card)
        if (card.suit == top?.suit || card.rank == top?.rank) {
            Messages.TableWon.show(currentPlayer)
            table.add(card)
            hand.remove(card)
            table.forEach { tableCard ->
                if (currentPlayer == Const.PLAYER) Score.player.count(tableCard)
                else Score.computer.count(tableCard)
            }
            lastWonTable = currentPlayer
            table.clear()
            scoreDisplay().also { println() }
        }
    }

    private fun dealCards(set: MutableSet<Card>) {
        repeat(Const.CARDS_DEAL) { deck.first().let {
                set.add(it)
                deck.remove(it)
            }
        }
    }

    private fun reset() {
        deck.clear()
        suits.forEach { suit -> ranks.forEach { rank -> deck.add(Card(rank, suit)) } }
        shuffle()
    }

    private fun shuffle() {
        val randomizedDeck = deck.shuffled()
        deck.clear()
        randomizedDeck.forEach { card -> deck.add(card) }
    }

    private fun scoreDisplay() {
        Messages.Points.show(Score.player.points.toString(), Score.computer.points.toString())
        Messages.Cards.show(Score.player.cards.toString(), Score.computer.cards.toString())
    }
}

fun main() {
    IndigoCardGame()
}