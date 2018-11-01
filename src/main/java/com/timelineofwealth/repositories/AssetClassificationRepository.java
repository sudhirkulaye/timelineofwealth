package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.AssetClassification;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
@EnableCaching
public interface AssetClassificationRepository extends JpaRepository<AssetClassification, Integer> {
    @Cacheable("AssetClassifications")
    public List<AssetClassification> findAll();
    public AssetClassification findByClassid(int classid);
//    public String findSubclassNameByClassid(int classid);
//    public String findClassNameByClassid(int classid);
}
