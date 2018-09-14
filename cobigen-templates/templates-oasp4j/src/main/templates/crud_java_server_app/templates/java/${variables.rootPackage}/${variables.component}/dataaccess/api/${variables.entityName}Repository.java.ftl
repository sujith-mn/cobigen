<#include '/functions.ftl'>
package ${variables.rootPackage}.${variables.component}.dataaccess.api;

import static com.querydsl.core.alias.Alias.$;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.querydsl.jpa.impl.JPAQuery;

import ${variables.rootPackage}.${variables.component}.common.api.${variables.entityName};
import ${variables.rootPackage}.${variables.component}.common.api.to.${variables.entityName}SearchCriteriaTo;
import io.oasp.module.jpa.dataaccess.api.QueryUtil;
import io.oasp.module.jpa.dataaccess.api.data.DefaultRepository;


/**
 * {@link DefaultRepository} for {@link ${variables.entityName}Entity}
  */
public interface ${variables.entityName}Repository extends DefaultRepository<${variables.entityName}Entity> {

<#list pojo.fields as field>
<#assign newFieldType=JavaUtil.boxJavaPrimitives(field.type?replace("[^<>,]+Entity","Long","r"))>
	<#if newFieldType?ends_with("Embeddable")><#assign newFieldType=newFieldType?replace("Embeddable","SearchCriteriaTo","r")></#if>
	<#assign newFieldType=newFieldType?replace("[^<>,]+Embeddable","SearchCriteriaTo","r")>
  /**
   * @param ${field.name} the {@link ${variables.entityName}#get${field.name?cap_first}() ${field.name}} to match.
   * @param pageable the {@link Pageable} to configure the paging.
   * @return the {@link Page} of {@link ${variables.entityName}Entity} objects that matched the search.
   */
  <#if !field.type?starts_with("List<") && !field.type?starts_with("Set<")>
        <#compress>    	
          <#if field.type?ends_with("Entity") && newFieldType=='Long'>
            @Query("SELECT ${variables.entityName?lower_case} FROM ${variables.entityName}Entity ${variables.entityName?lower_case}" //
            + " WHERE ${variables.entityName?lower_case}.${field.name}Id = :${field.name}Id")
            Page<${variables.entityName}Entity> findBy${field.name?cap_first}Id(@Param("${field.name}Id") ${newFieldType} ${field.name}Id, Pageable pageable);


          <#else>
            @Query("SELECT ${variables.entityName?lower_case} FROM ${variables.entityName}Entity ${variables.entityName?lower_case}" //
            + " WHERE ${variables.entityName?lower_case}.${field.name} = :${field.name}")
            Page<${variables.entityName}Entity> findBy${field.name?cap_first}(@Param("${field.name}") ${field.type} ${field.name}, Pageable pageable);
          </#if>          
    	</#compress>
  </#if>
</#list>

  /**
   * @param criteria the {@link ${variables.entityName}SearchCriteriaTo} with the criteria to search.
   * @return the {@link Page} of the {@link ${variables.entityName}Entity} objects that matched the search.
   */
  default Page<${variables.entityName}Entity> findByCriteria(${variables.entityName}SearchCriteriaTo criteria) {

    ${variables.entityName}Entity alias = newDslAlias();
    JPAQuery<${variables.entityName}Entity> query = newDslQuery(alias);
    <#list pojo.fields as field>
      <#compress>
        <#assign newFieldType=field.type?replace("[^<>,]+Entity","Long","r")>
        <#if newFieldType?ends_with("Embeddable")><#assign newFieldType=newFieldType?replace("Embeddable","SearchCriteriaTo","r")></#if>
          <#assign newFieldType=newFieldType?replace("[^<>,]+Embeddable","SearchCriteriaTo","r")>
          <#assign fieldCapName=field.name?cap_first>
      </#compress>
        <#if !field.type?starts_with("List<") && !field.type?starts_with("Set<")>
          <#compress>          
              <#if field.type?ends_with("Entity") && newFieldType=='Long'>
                ${newFieldType} ${field.name} = criteria.${OaspUtil.resolveIdGetter(field,false,"")};
                if ((${field.name} != null)) {
                  QueryUtil.get().whereString(query, $(alias.get${field.name?cap_first}()), (String) ${field.name}, criteria.get${field.name?cap_first}Option());
                }                                    
              <#else>
                ${field.type} ${field.name} = criteria.<#if field.type=='boolean'>is${fieldCapName}()<#else>${OaspUtil.resolveIdGetter(field,false,"")}</#if>;
                if ((${field.name} != null) <#if field.type=='String'> && !${field.name}.isEmpty()</#if>) {
                  QueryUtil.get().whereString(query, $(alias.get${field.name?cap_first}()), (String) ${field.name}, criteria.get${field.name?cap_first}Option());
                } 
              </#if>        
      </#compress>
    </#if>
    </#list>
    return QueryUtil.get().findPaginated(criteria.getPageable(), query, false);
  }

} // booleans !