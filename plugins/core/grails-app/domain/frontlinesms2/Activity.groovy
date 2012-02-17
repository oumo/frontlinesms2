package frontlinesms2

import java.util.Date

class Activity extends MessageOwner {
	String name
	String sentMessageText
	Date dateCreated
	static transients = ['liveMessageCount']

	static constraints = {
		name(blank: false, nullable: false)
		sentMessageText(nullable:true)
	}
	
	def getActivityMessages(getOnlyStarred=false, getSent=true) {
		Fmessage.owned(this, getOnlyStarred, getSent)
	}
	
	def archive() {
		this.archived = true
		def messagesToArchive = Fmessage.owned(this, false, true)?.list()
		messagesToArchive.each { it?.archived = true }
	}
	
	def unarchive() {
		this.archived = false
		Fmessage.owned(this, false, true)?.list()*.each { it?.archived = false }
	}
	
	def getLiveMessageCount() {
		def m = Fmessage.findAllByMessageOwnerAndIsDeleted(this, false)
		m ? m.size() : 0
	}
}
