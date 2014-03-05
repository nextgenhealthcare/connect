<%@include file="/WEB-INF/jsp/common/taglibs.jsp"%>

<s:layout-render name="/WEB-INF/jsp/common/layoutmain.jsp" title="Dashboard Statistics">
    <s:layout-component name="head">
        <link href="${contextPath}/css/jquery.treeTable.css" rel="stylesheet" type="text/css" />

        <!-- Hack to fix CSS spacing conflict between tablesorter and bootstrap -->
        <style type="text/css">
            .header div {
            	float: left;
            }
        </style>
    </s:layout-component>

    <s:layout-component name="body">
        <div id="errorAlert" class="alert alert-danger hide fade in" data-alert="alert" style= "margin-left:auto; margin-right:auto;">
            <a class="close" data-dismiss="alert" href="#">&times;</a>
            <p><strong>Error connecting to Server</strong>. Refresh the page or <a href="Index.action">Login</a></p>
        </div>

        <ul id="myTab" class="nav nav-tabs">
            <li class="active"><a id="current" href="#" data-toggle="tab">Current Statistics</a></li>
            <li><a id="lifetime" href="#" data-toggle="tab">Lifetime Statistics</a></li>
        </ul>

        <table class="table table-striped table-bordered table-condensed tablesorter" style="width: 98%;" id="treeTable">
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Status</th>
                    <th>Received</th>
                    <th>Filtered</th>
                    <th>Queued</th>
                    <th>Sent</th>
                    <th>Errored</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${actionBean.dashboardStatusList}" var="dashboardStatus" varStatus="status">
                    <tr id="node-${status.index}">
                        <td class="parent">${dashboardStatus.name}</td>
                        <td>${dashboardStatus.state}</td>
                        <td>${dashboardStatus.statistics[RECEIVED]}</td>
                        <td>${dashboardStatus.statistics[FILTERED]}</td>
                        <td>${dashboardStatus.queued}</td>
                        <td>${dashboardStatus.statistics[SENT]}</td>
                        <td class="errors" <c:if test="${dashboardStatus.statistics[ERROR] != 0}"> style="background-color:LightPink;" </c:if>> ${dashboardStatus.statistics[ERROR]}</td>
                    </tr>

                    <c:forEach items="${dashboardStatus.childStatuses}" var="childStatus">
                        <c:set var="childName" value="${childStatus.name}" />
                        <c:set var="trimName" value="${fn:replace(childName,' ','-')}" />

                        <tr id="${trimName}-${status.index}" class="child-of-node-${status.index} expand-child">
                            <td class="child">${childStatus.name}</td>
                            <td>${childStatus.state}</td>
                            <td>${childStatus.statistics[RECEIVED]}</td>
                            <td>${childStatus.statistics[FILTERED]}</td>
                            <td>${childStatus.queued}</td>
                            <td>${childStatus.statistics[SENT]}</td>
                            <td class="errors" <c:if test="${childStatus.statistics[ERROR] != 0}"> style="background-color:LightPink;" </c:if>> ${childStatus.statistics[ERROR]}</td>
                        </tr>
                    </c:forEach>
                </c:forEach>
            </tbody>
        </table>
    </s:layout-component>

    <s:layout-component name="scripts">
        <script type="text/javascript" src="${contextPath}/js/jquery.treeTable.js"></script>
        <script type="text/javascript" src="${contextPath}/js/persist-min.js"></script>
        <script type="text/javascript" src="${contextPath}/js/jquery.tablesorter.min.js"></script>
        <script type="text/javascript" src="${contextPath}/js/jquery.tablesorter.widgets.min.js"></script>

        <!-- Script to update stats dynamically every x seconds via ajax -->
        <script type="text/javascript">
            var timeout = 5000;
            var showLifetimeStats = false;
            var updateTimeout;

            function updateStats() {
                $.get('DashboardStatistics.action?getStats&showLifetimeStats=' + showLifetimeStats, function(nodes) {
					var showAlert = ${actionBean.showAlert};

                    // Refresh
                    if (nodes == "0") {
                        document.location.reload(true);
                        return;
                    }

                    if (showAlert) {
                        $("#errorAlert").removeClass('hide');
                    } else {
                        $("#errorAlert").addClass('hide');
                    }

                    for (var i = 0; i < nodes.length; i++) {
                        var node = nodes[i];
                        var row = $('#' + node.id);

                        checkAndUpdateError(node);

                        row.children().eq(1).text(node.status);
                        row.children().eq(2).text(node.received);
                        row.children().eq(3).text(node.filtered);
                        row.children().eq(4).text(node.queued);
                        row.children().eq(5).text(node.sent);
                        row.children().eq(6).text(node.errored);
                    }
                    
                    $('.result').html(nodes);
                }, "json");
                
                updateTimeout = setTimeout(updateStats, timeout);
            }

            function checkAndUpdateError(node) {
                var row = $('#' + node.id);

                if (node.errored > 0) {
                    row.children().eq(6).css("background-color", "LightPink");
                } else {
                    // Even rows paint cell background transparent
                    if (row.index() % 2 == 0) {
                        row.children().eq(6).css("background-color", "#F9F9F9");
                    }

                    // Odd rows paint cell background grey
                    else {
                        row.children().eq(6).css("background-color", "transparent");
                    }
                }
            }

            $(document).ready(function() {
                updateTimeout = setTimeout(updateStats, timeout);
            });
        </script>

        <!-- Enable Bootstrap Javascript Tabs -->
        <script type="text/javascript">
            $(document).ready(function() {
                $('#myTab a').click(function(e) {
                    e.preventDefault();

                    if ($(this).attr("id") == "current") {
                        showLifetimeStats = false;
                        clearTimeout(updateTimeout);
                    } else {
                        showLifetimeStats = true;
                        clearTimeout(updateTimeout);
                    }
                    updateStats();
                    $(this).tab('show');
                });
            });
        </script>

        <!-- TreeTable plugin -->
        <script type="text/javascript">
            $(document).ready(function() {
                $("#treeTable").treeTable({
                    initialState : "collapsed",
                    clickableNodeNames : true,
                    persist : true
                // Persist node expanded/collapsed state
                });
            });
        </script>

        <!-- TableSorter plugin -->
        <script type="text/javascript">
            $(document).ready(function() {
                $("#treeTable").tablesorter({
                    // Persist sorting state
                    widgets : [ "saveSort" ],

                    // Override tablesorter CSS to use bootstrap styling
                    cssHeader : "header",
                    cssAsc : "headerSortDown",
                    cssDesc : "headerSortUp"
                });
            });
        </script>

        <!-- Script to highlight errored cells pink whenever value > 0 -->
        <script type="text/javascript">
            $(document).ready(function() {
                if (Number($(this).text()) > 0) {
                    $(this).css("background-color", "LightPink");
                }
            });
        </script>

        <!-- Hack to fix CSS extra arrow conflict between tablesorter and bootstrap -->
        <script type="text/javascript">
            $(document).ready(function() {
                $("#body table thead tr").removeAttr("class");
            });
        </script>
    </s:layout-component>
</s:layout-render>