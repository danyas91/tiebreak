package com.djasik.tiebreak.domain.model

data class Team(
    val id: TeamId,
    val stats: TeamStats,
)

object Teams {
    val Dragons = Team(
        id = TeamId.DRAGONS,
        stats = TeamStats(
            attack = 8,
            defense = 6,
            stamina = 7,
        ),
    )

    val Tigers = Team(
        id = TeamId.TIGERS,
        stats = TeamStats(
            attack = 7,
            defense = 8,
            stamina = 6,
        ),
    )
}

fun TeamId.toTeam(): Team =
    when (this) {
        TeamId.DRAGONS -> Teams.Dragons
        TeamId.TIGERS -> Teams.Tigers
    }

fun TeamId.opponent(): TeamId =
    when (this) {
        TeamId.DRAGONS -> TeamId.TIGERS
        TeamId.TIGERS -> TeamId.DRAGONS
    }
