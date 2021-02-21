package com.dbflow5.models

import com.dbflow5.TestDatabase
import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.ForeignKeyAction
import com.dbflow5.annotation.Index
import com.dbflow5.annotation.IndexGroup
import com.dbflow5.annotation.NotNull
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.annotation.TypeConverter
import com.dbflow5.structure.BaseModel


@TypeConverter
class MutableSetTypeConverter : com.dbflow5.converter.TypeConverter<String, MutableSet<String>>() {
    override fun getDBValue(model: MutableSet<String>?): String? {
        return model?.joinToString()
    }

    override fun getModelValue(data: String?): MutableSet<String>? {
        return data?.split("")?.toMutableSet()
    }
}


/*
 * A quiz, consisting primarily of a question from the current user, and a set of responses from
 * prospects and/or prior prospects that the user has acted upon. Though the author ID is needed on the server, the
 * client has no need of it. Instead, just the canonical quiz identifier is used.
 * It is envisioned that the resolvedResponses may be provided only when an additional parameter is
 * specified in the request.
 */
@Table(database = TestDatabase::class, allFields = true, useBooleanGetterSetters = false,
    indexGroups = [IndexGroup(number = 1, name = "modified")])
open internal class ProspectQuiz : BaseModel {
    @NotNull
    @PrimaryKey
    lateinit var ID: String

    @NotNull
    var question: String

    @NotNull
    var pendingResponseCount: Int

    @NotNull
    var resolvedResponseCount: Int

    @NotNull
    var newResponseCount: Int

    // We have a list of prospect IDs that have been invited but not answered. Just IDs to be used
    // with determining who can still be added.
    @NotNull
    @Column(typeConverter = MutableSetTypeConverter::class)
    var pendingUnanswered: MutableSet<String>

    @Index(indexGroups = [1])
    @Column(defaultValue = "1L")
    var modifiedDate: Long? = null

    constructor(quizID: String) : this() {
        ID = quizID
    }

    constructor() {
        question = ""
        pendingResponseCount = 0
        resolvedResponseCount = 0
        newResponseCount = 0
        pendingUnanswered = mutableSetOf()
    }
}

/**
 * An element of a quiz, consisting of a user the quiz targeted and his/her response
 * Status is only used on the quiz full view, in which profiles that have been rejected or matched
 * will be included in the quiz, but shown separately. In the quiz full view, a participant whose
 * account is deactivated or deleted should never be visible. When a quiz is refreshed and a participant
 * deleted or deactivated the account, the entry for the deleted user will disappear from the response.
 */
@Table(database = TestDatabase::class, allFields = true, useBooleanGetterSetters = false,
    indexGroups = [IndexGroup(number = 1, name = "quizid_answerts")])
open internal class ProspectQuizEntry : BaseModel {
    @PrimaryKey
    @NotNull
    lateinit var profileID: String

    @PrimaryKey
    @NotNull
    @Index(indexGroups = [1])
    @ForeignKey(stubbedRelationship = true,
        tableClass = ProspectQuiz::class,
        onDelete = ForeignKeyAction.CASCADE)
    lateinit var quizID: String

    //@ForeignKey(saveForeignKeyModel = true) var photo: PhotoMedia?
    var text: String?
    var participantStatus: QuizParticipantStatus?

    @NotNull
    var name: String

    @NotNull
    @Index(indexGroups = [1])
    var answerEpoch: Long

    // naming thing (`new`)
    //@NotNull @JvmField @Column(typeConverter = ObservableBooleanTypeConverter::class)
    //var neww: ObservableBoolean = ObservableBoolean(false)
    /*var new: Boolean
        get() = neww.get()
        set(value) { neww.set(value) }*/

    constructor() {
        //photo = null
        text = null
        participantStatus = null
        name = ""
        //new = false
        answerEpoch = 0
    }
}

enum class QuizParticipantStatus {
    Pending,
    Rejected,
    Connected,
    ;

    companion object {
        fun fromCode(code: Int): QuizParticipantStatus {
            return when (code) {
                0 -> Pending
                1 -> Rejected
                2 -> Connected
                else -> throw IllegalArgumentException("Invalid raw int for QuizParticipantStatus")
            }
        }
    }
}