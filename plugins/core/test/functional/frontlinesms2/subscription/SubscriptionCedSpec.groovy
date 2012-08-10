package frontlinesms2.subscription

import frontlinesms2.*
import frontlinesms2.page.*
import frontlinesms2.message.PageMessageInbox

class SubscriptionCedSpec extends grails.plugin.geb.GebSpec  {
	def "can launch subscription wizard from create new activity link" () {
		when:
			to PageMessageInbox
			bodyMenu.newActivity.click()
		then:
			waitFor { at CreateActivityDialog}
		when:
			subscription.click()
		then:
			waitFor { at SubscriptionDialog }
	}

	def "Can create a new subscription" () {
		when:
			to PageMessageInbox
			bodyMenu.newActivity.click()
		then:
			waitFor {at CreateActivityDialog}
		when:
			subscription.click()
		then:
			waitFor {at SubscriptionDialog}
		when:
			group.addToGroup Group.findByName('Friends').id.toString()
			keywordText = 'friend'
			enableJoinKeyword.click()
			joinAliases = 'join, start'
			enableLeaveKeyword.click()
			leaveAliases = 'leave, stop'
			next.click()
		then:
			waitFor {autoreply.displayed}
		when:
			enableJoinAutoreply.click()
			joinAutoreplyText = "You have been successfully subscribed to Friends group"
			enableLeaveAutoreply.click()
			leaveAutoreplyText = "You have been unsubscribed from Friends group"
		then:
			waitFor { confirm.subscriptionName.displayed }
		when:
			confirm.subscriptionName.value("Friends subscription")
			submit.click()
		then:
			waitFor { summary.message.displayed }
	}

	def "Can edit an existing subscription"() {
		setup:
			//TODO set up a friends subscription
		when:
			to PageMessageInbox
			bodyMenu.activityLinks[].click()//click on friends subscription
		then:
			waitFor { at PageMessageSubscription }
		when:
			moreActions.value("edit").click()
		then:
			waitFor { at EditSubscriptionDialog }
		when:
			group.addToGroup Group.findByName('Not cats').id.toString()
			keywordText = 'nonecats'
			next.click()
		then:
			waitFor {autoreply.displayed}
		when:
			enableJoinAutoreply.click()
			joinAutoreplyText = "You have been successfully subscribed to Friends group"
			enableLeaveAutoreply.click()
			leaveAutoreplyText = "You have been unsubscribed from Friends group"
		then:
			waitFor { confirm.subscriptionName.displayed }
		when:
			confirm.subscriptionName.value("Not cats subscription")
			submit.click()
		then:
			waitFor { summary.message.displayed }
	}
}
