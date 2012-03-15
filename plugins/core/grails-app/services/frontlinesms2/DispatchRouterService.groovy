package frontlinesms2

import org.apache.camel.Exchange
import org.apache.camel.Header

/** This is a Dynamic Router */
class DispatchRouterService {
	def camelContext

	int counter = 0

	/**
	 * Slip should return the list of ______ to forward to, or <code>null</code> if
	 * we've done with it.
	 */
	def slip(Exchange exchange, @Header(Exchange.SLIP_ENDPOINT) String previous, @Header('fconnection') String target) {
		println "DispatchRouterService.slip() : Routing exchange $exchange with previous endpoint $previous and target fconnection $target"
		if(previous) {
			// We only want to pass this message to a single endpoint, so if there
			// is a previous one set, we should exit the slip.
			println "DispatchRouterService.slip() : Exchange has previous endpoint from this slip.  Returning null."
			return null
		} else if(target) {
			println "DispatchRouterService.slip() : Target is set, so forwarding exchange to fconnection $target"
			println "DispatchRouterService.slip() : Exchange properties: $exchange.properties"
			return "seda:out-$target"
		} else {
			println "DispatchRouterService.slip() : Routes available: ${camelContext.routes*.id}"
			def filteredRouteList = filter(camelContext.routes, { it.id.startsWith('out-') })
			if(filteredRouteList.size > 0) {
				println "DispatchRouterService.slip() : Routes available: ${filteredRouteList*.id}"
				println "DispatchRouterService.slip() : Counter has counted up to $counter"
				def connectionId = filteredRouteList[++counter % filteredRouteList.size].id
				println "DispatchRouterService.slip() : Sending with connection: $connectionId"
				println "DispatchRouterService.slip() : Setting header 'fconnection' to $connectionId"
				exchange.out.headers.fconnection = connectionId
				def routeName = "seda:$connectionId"
				println "DispatchRouterService.slip() : Routing to $routeName"
				return routeName
			} else {
				// TODO may want to queue for retry here, after incrementing retry-count header
				throw new RuntimeException("No outbound route available for dispatch.")
			}
		}
	}

	def filter(List l, Closure c) {
		def r = []
		l.each {
			if(c(it)) r << it
		}
		r
	}

	def handleCompleted(Exchange x) {
		println "DispatchRouterService.handleCompleted() : ENTRY"
		updateDispatch(x, DispatchStatus.SENT)
		println "DispatchRouterService.handleCompleted() : EXIT"
	}

	def handleFailed(Exchange x) {
		println "DispatchRouterService.handleFailed() : ENTRY"
		updateDispatch(x, DispatchStatus.FAILED)
		println "DispatchRouterService.handleFailed() : EXIT"
	}
	
	private Dispatch updateDispatch(Exchange x, s) {
		def id = x.in.getHeader('frontlinesms.dispatch.id')
		Dispatch d
		if(x.in.body instanceof Dispatch) {
			d = x.in.body
			d.refresh()
		} else {
			d = Dispatch.get(id)
		}
		
		println "DispatchRouterService.updateDispatch() : dispatch=$d" 
		
		if(d) {
			d.status = s
			if(s == DispatchStatus.SENT) d.dateSent = new Date()

			try {
				d.save(failOnError:true, flush:true)
			} catch(Exception ex) {
				log.error("Could not save dispatch $d with message $d.message", ex)
			}
		} else log.info("No dispatch found for id: $id")
	}
}
