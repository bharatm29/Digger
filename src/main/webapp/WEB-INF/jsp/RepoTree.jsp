<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Repo Tree</title>
    <link rel="stylesheet" type="text/css" href="/css/repo.css">
</head>
<body>
<h2>Repository Tree</h2>

<ul class="repo-list">
    <c:forEach var="obj" items="${objs}">
        <li class="${obj.type}" onclick="this.querySelector('form').submit();">
            <form action="/show" method="post" style="margin:0;">
                <input type="hidden" name="download_url" value="${obj.download_url}"/>
                <input type="hidden" name="url" value="${obj.url}"/>
                <input type="hidden" name="name" value="${obj.name}"/>
                <input type="hidden" name="type" value="${obj.type}"/>
                <span class="icon">
                    <c:choose>
                        <c:when test="${obj.type == 'dir'}">&#128193;</c:when> <%--directory icon--%>
                        <c:otherwise>&#128196;</c:otherwise> <%--file icon--%>
                    </c:choose>
                </span>
                <span class="file-link">${obj.name}</span>
            </form>
        </li>
    </c:forEach>
</ul>

<form action="/downloadDir" method="post" style="margin:0;">
    <input type="hidden" name="repo" value="${repo}"/>
    <button type="submit" name="url" value="${root}">Download</button>
</form>

<c:if test="${not empty error}">
    <div class="error">${error}</div>
</c:if>

</body>
</html>
