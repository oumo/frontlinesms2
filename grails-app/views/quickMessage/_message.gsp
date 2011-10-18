<div id="tabs-1" class="${configureTabs.contains('tabs-1') ? '' : 'hide'}">
	<label class="header" for="messageText">Enter message</label><br />
	<g:textArea name="messageText" value="${messageText}" rows="5" cols="40"/>
	<span id="character-count">0 characters (1 SMS message)</span> 
</div>

<script>
	$("#messageText").live({
		blur: function() {
			var value = $(this).val();
			if(value) {
				$("#confirm-message-text").html(value);
			}
			else {
				$("#confirm-message-text").html("none");
			}
		},
		keyup: function() {
			var value = $(this).val();
			if(value.length > 140) {
				$.get(url_root + 'message/countMessageCharacters', {message: value}, function(data) {
					$("#character-count").html(data);
				});
			}
			else {
				$("#character-count").html(value.length + " characters (1 SMS message)");
			}
		}	
	})
</script>