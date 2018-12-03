package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.MutualFundUniverse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
@EnableCaching
public interface MutualFundUniverseRepository extends JpaRepository<MutualFundUniverse, Long> {

//    @Cacheable("SchemeCodes")
//    @Query("select fund.schemeCode from MutualFundUniverse fund")
//    public List<Long> findAllSchemeCodes();

    //@Cacheable("FundHouses")
    @Query("select distinct fund.fundHouse from MutualFundUniverse fund")
    List<String> findDistinctFundHouse();

    @Cacheable("SchemeById")
    public MutualFundUniverse findBySchemeCode(Long schemeCode);
    public int countBySchemeCode(Long schemeCode);
    public List<MutualFundUniverse> findByIsinDivPayoutIsinGrowth(String isinDivPayoutOrIsinGrowth);

    //@Cacheable(value = "SchemeNamesByFundHouse")
    @Query(value = "select fund from MutualFundUniverse fund where fund.fundHouse= :fundHouse")
    public List<MutualFundUniverse> findSchemeNamesByFundHouse(@Param("fundHouse")String fundHouse, Sort sort);
    //@Cacheable(value = "SchemeNamesByFundHouseAndPlan")
    @Query(value = "select fund from MutualFundUniverse fund where fund.fundHouse= :fundHouse and fund.directRegular=:directRegular")
    public List<MutualFundUniverse> findSchemeNamesByFundHouse(@Param("fundHouse")String fundHouse, @Param("directRegular")String directRegular, Sort sort);
    //@Cacheable(value = "SchemeNamesByFundHouseAndPlanAndOption")
    @Query(value = "select fund from MutualFundUniverse fund where fund.fundHouse= :fundHouse and fund.directRegular=:directRegular and fund.dividendGrowth=:dividendGrowth")
    public List<MutualFundUniverse> findSchemeNamesByFundHouse(@Param("fundHouse")String fundHouse, @Param("directRegular")String directRegular, @Param("dividendGrowth")String dividendGrowth, Sort sort);
    @Query(value = "select fund from MutualFundUniverse fund where fund.fundHouse= :fundHouse and fund.category like :category% and fund.dividendGrowth='Growth'")
    public List<MutualFundUniverse> findSchemeNamesByFundHouseAndCategory(@Param("fundHouse")String fundHouse, @Param("category")String cateogry, Sort sort);
    public List<MutualFundUniverse> findAllByFundHouseAndCategory(String fundHouse, String cateogry);


    public List<MutualFundUniverse> findAllBySchemeNamePartIgnoreCaseContaining(String schemeNamePart);


}
