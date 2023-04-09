$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	/* 发送AJAX请求之前，需要将CSRF令牌设置到请求的消息头中 */
	// let token = $("meta[name='_csrf']").attr("content");
   	// let header = $("meta[name='_csrf_header']").attr("content");
  	// $(document).ajaxSend(function(e, xhr, options){
    //    xhr.setRequestHeader(header, token);
   	// });

	/* Get the title and content */
	let title = $("#recipient-name").val();
	let content = $("#message-text").val();

	/* Sent the request */
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title" : title, "content" : content},
		(data) => {
			data = $.parseJSON(data)
			$("#hintBody").text(data.msg)
			/* Open the prompt box */
			$("#hintModal").modal("show")
			setTimeout(() => {
				$("#hintModal").modal("hide");
				if(data.code === 200) {
					/* Reload the current page to display the newly added post */
					window.location.reload()
				}
			}, 2000)
		}
	)

	$("#hintModal").modal("show");
	setTimeout(function(){
		$("#hintModal").modal("hide");
	}, 2000);
}