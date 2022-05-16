$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");

	// 将用户所要发送的私信添加到服务端
	let receiverName = $("#recipient-name").val();
	let content = $("#message-text").val();

	$.post(
		CONTEXT_PATH + "/letter/send",
		{"receiverName": receiverName, "content": content},
		function (data) {
			data = $.parseJSON(data);
			// 根据所返回的数据来显示相应的信息
			if (data.code === 200) {
				$("#hintBody").text("Message is send successfully");
			} else {
				$("#hintBody").text(data.msg);
			}

			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	);
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}