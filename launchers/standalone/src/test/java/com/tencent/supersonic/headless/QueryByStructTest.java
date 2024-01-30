package com.tencent.supersonic.headless;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.tencent.supersonic.auth.api.authentication.pojo.User;
import com.tencent.supersonic.common.pojo.Filter;
import com.tencent.supersonic.common.pojo.QueryColumn;
import com.tencent.supersonic.common.pojo.enums.FilterOperatorEnum;
import com.tencent.supersonic.common.pojo.exception.InvalidPermissionException;
import com.tencent.supersonic.headless.api.pojo.request.QueryStructReq;
import com.tencent.supersonic.headless.api.pojo.response.SemanticQueryResp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class QueryByStructTest extends BaseTest {

    @Test
    public void testSumQuery() throws Exception {
        QueryStructReq queryStructReq = buildQueryStructReq(null);
        SemanticQueryResp semanticQueryResp = queryService.queryByReq(queryStructReq, User.getFakeUser());
        assertEquals(1, semanticQueryResp.getColumns().size());
        QueryColumn queryColumn = semanticQueryResp.getColumns().get(0);
        assertEquals("访问次数", queryColumn.getName());
        assertEquals(1, semanticQueryResp.getResultList().size());
    }

    @Test
    public void testGroupByQuery() throws Exception {
        QueryStructReq queryStructReq = buildQueryStructReq(Arrays.asList("department"));
        SemanticQueryResp result = queryService.queryByReq(queryStructReq, User.getFakeUser());
        assertEquals(2, result.getColumns().size());
        QueryColumn firstColumn = result.getColumns().get(0);
        QueryColumn secondColumn = result.getColumns().get(1);
        assertEquals("部门", firstColumn.getName());
        assertEquals("访问次数", secondColumn.getName());
        assertNotNull(result.getResultList().size());
    }

    @Test
    public void testFilterQuery() throws Exception {
        QueryStructReq queryStructReq = buildQueryStructReq(Arrays.asList("department"));
        List<Filter> dimensionFilters = new ArrayList<>();
        Filter filter = new Filter();
        filter.setName("部门");
        filter.setBizName("department");
        filter.setOperator(FilterOperatorEnum.EQUALS);
        filter.setValue("HR");
        dimensionFilters.add(filter);
        queryStructReq.setDimensionFilters(dimensionFilters);

        SemanticQueryResp result = queryService.queryByReq(queryStructReq, User.getFakeUser());
        assertEquals(2, result.getColumns().size());
        QueryColumn firstColumn = result.getColumns().get(0);
        QueryColumn secondColumn = result.getColumns().get(1);
        assertEquals("部门", firstColumn.getName());
        assertEquals("访问次数", secondColumn.getName());
        assertEquals(1, result.getResultList().size());
        assertEquals("HR", result.getResultList().get(0).get("department").toString());
    }

    @Test
    public void testCacheQuery() throws Exception {
        QueryStructReq queryStructReq1 = buildQueryStructReq(Arrays.asList("department"));
        QueryStructReq queryStructReq2 = buildQueryStructReq(Arrays.asList("department"));
        SemanticQueryResp result1 = queryService.queryByReq(queryStructReq1, User.getFakeUser());
        SemanticQueryResp result2 = queryService.queryByReq(queryStructReq2, User.getFakeUser());
        assertEquals(result1, result2);
    }

    @Test
    public void testAuthorization() {
        User alice = new User(2L, "alice", "alice", "alice@email", 0);
        QueryStructReq queryStructReq1 = buildQueryStructReq(Arrays.asList("department"));
        assertThrows(InvalidPermissionException.class,
                () -> queryService.queryByReq(queryStructReq1, alice));
    }
}
