<g:remoteLink controller="radioShow" action="wordCloudStats" name="show-wordcloud-btn" params="${[id:ownerInstance?.id, messageSection:messageSection]}" class="btn" onSuccess="showWordCloud(data)"><g:message code="wordcloud.show.cloud"/></g:remoteLink>
<a onclick="showMessages()" id="show-messages-btn" class="btn" style="display:none;"><g:message code="wordcloud.show.messages"/></a>