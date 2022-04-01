$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	// Get the title and content
	let title = $("#recipient-name").val();
	let content = $("#message-text").val();

	// Sent the request
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title" : title, "content" : content},
		(data) => {
			data = $.parseJSON(data)
			$("#hintBody").text(data.msg)
			$("#hintModal").modal("show")
			setTimeout(() => {
				$("#hintModal").modal("hide");
				if(data.code = 200) {
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