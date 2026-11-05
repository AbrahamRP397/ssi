<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Redirige siempre a /login (el LoginServlet decide si ya hay sesión)
    response.sendRedirect(request.getContextPath() + "/login");
%>