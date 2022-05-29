/* 在页面加载完毕之后向页面元素中绑定事件 */
$(() => {
    $("#topBtn").click(setTop); // 向topBtn元素绑定setTop函数
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
})

const giveLike = (btn, entityType, entityId, entityUserId, postId) => {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType": entityType, "entityId": entityId, "entityUserId":entityUserId, "postId": postId},
        (data) => {
            data = $.parseJSON(data + "");
            if (data.code === 200) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus === 1 ? "已赞" : "赞");
            } else {
                alert(data.msg);
            }
        }
    )
}

/* setTop函数用于执行置顶操作 */
const setTop = () => {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        (data) => { // 对相应数据进行处理
            let type = typeof data
            if (type === "string") {
                data = $.parseJSON(data);
            } else {
                data = $.parseJSON(JSON.stringify(data));
            }

            if (data.code === 200) {
                $('#topBtn').attr("disabled", "disabled") // 在设置为置顶之后将按钮禁用
            } else {
                alert(data.msg);
            }
        }
    )
}

/* setWonderful函数用于执行置顶操作 */
const setWonderful = () => {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        (data) => { // 对相应数据进行处理
            let type = typeof data
            if (type === "string") {
                data = $.parseJSON(data);
            } else {
                data = $.parseJSON(JSON.stringify(data));
            }

            if (data.code === 200) {
                $('#wonderfulBtn').attr("disabled", "disabled") // 在设置为置顶之后将按钮禁用
            } else {
                alert(data.msg);
            }
        }
    )
}

/* setDelete函数用于执行删除操作 */
const setDelete = () => {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        (data) => { // 对相应数据进行处理
            let type = typeof data
            if (type === "string") {
                data = $.parseJSON(data);
            } else {
                data = $.parseJSON(JSON.stringify(data));
            }

            if (data.code === 200) {
                location.href = CONTEXT_PATH + "/index"; // 在删除完帖子之后跳转到首页
            } else {
                alert(data.msg);
            }
        }
    )
}