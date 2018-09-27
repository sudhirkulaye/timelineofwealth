package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "member")
public class Member implements Serializable {

    private long memberid;
    private String firstName;
    private String middleName;
    private String lastName;
    private String relationship;
    /*
      <select class="form-control">
          <option selected>Self</option>
          <option>Wife</option>
          <option>Husband</option>
          <option>Father</option>
          <option>Mother</option>
          <option>Son</option>
          <option>Daughter</option>
          <option>Brother</option>
          <option>Sister</option>
          <option>In-laws</option>
          <option>Other</option>
       </select>
     */
    private Date birthDate;
    private String gender; //M: Male, F: Female, O: Other
    private String maritalStatus; //Single, Married, Divorcee, Widow
    private String email;
    private String cellNo;
    private String earningStatus; // Dependent, Earning, Not-Earning
    private String profession; // Profession Category
    /*
      <select class="form-control" ng-model="memberForm.profession">
            <option>Secured Govt. Job</option>
            <option>Stable Pvt. Sector Job</option>
            <option>Unstable Pvt. Sector Job</option>
            <option>Large Size Business</option>
            <option>Medium Size Business</option>
            <option>Small Size Business</option>
            <option>Professional/Self Employed</option>
            <option>Athlete/Actor/Model</option>
            <option>Pensioner</option>
            <option>Retired No Pension</option>
            <option>Housewife</option>
            <option>HNI</option>
            <option>Other</option>
        </select>
     */
    private String industry;
    /*
     <select>
        <option selected></option>
          <optgroup label="Discretionary">
              <option>Auto Components</option>
              <option>Automobiles</option>
              <option>Cons. Durables Leisure Products</option>
              <option>Diversified Consumer Services</option>
              <option>Hotels, Restaurants & Leisure</option>
              <option>Household Durables</option>
              <option>Media</option>
              <option>Retailing - Distributors</option>
              <option>Retailing - Internet & Catalog Retail</option>
              <option>Retailing - Multiline Retail</option>
              <option>Retailing - Specialty Retail</option>
              <option>Textiles, Apparel, Luxury Goods</option>
          </optgroup>
          <optgroup label="Energy">
              <option>Equipment & Services</option>
              <option>Oil, Gas & Coal</option>
          </optgroup>
          <optgroup label="Financials">
              <option>Banks</option>
              <option>Consumer Finance</option>
              <option>Diversified Financial Services</option>
              <option>Financials Capital Markets</option>
              <option>Insurance</option>
              <option>Mortgage Finance</option>
              <option>Real Estate Invest Trust-REIT</option>
              <option>Real Estate Mgmt & Devpt</option>
          </optgroup>
          <optgroup label="Health Care">
              <option>Biotechnology</option>
              <option>Health Care Equipt & Supplies</option>
              <option>Health Care Service Providers</option>
              <option>Health Care Technology</option>
              <option>Life Sciences Tools & Services</option>
              <option>Pharmaceuticals</option>
          </optgroup>
          <optgroup label="Industrials">
              <option>Cap Goods - Aerospace & Defense</option>
              <option>Cap Goods - Building Products</option>
              <option>Cap Goods - Conglomerates</option>
              <option>Cap Goods - Construction & Eng.</option>
              <option>Cap Goods - Electrical Equipment</option>
              <option>Cap Goods - Machinery</option>
              <option>Cap Goods - Trading, Distributors</option>
              <option>Commercial Services & Supplies</option>
              <option>Professional Services</option>
              <option>Transportation - Airlines</option>
              <option>Transportation - Infra</option>
              <option>Transportation - Logistics</option>
              <option>Transportation - Marine</option>
              <option>Transportation - Road & Rail</option>
          </optgroup>
          <optgroup label="Materials">
              <option>Chemicals</option>
              <option>Construction Materials</option>
              <option>Containers & Packaging</option>
              <option>Metals & Mining</option>
              <option>Paper & Forest Products</option>
          </optgroup>
          <optgroup label="Staples">
              <option>Beverages</option>
              <option>Food & Staples Retailing</option>
              <option>Food Products</option>
              <option>Household Products</option>
              <option>Personal Products</option>
              <option>Tobacco</option>
          </optgroup>
          <optgroup label="Technology">
              <option>Communications Equipment</option>
              <option>Electronic Equipt, Instru, Comp</option>
              <option>Hardware, Storage & Peripherals</option>
              <option>Internet Software & Services</option>
              <option>IT Services</option>
              <option>Semiconductor Equipment</option>
              <option>Software</option>
          </optgroup>
          <optgroup label="Telecom">
              <option>Diversified Telecom Services</option>
              <option>Wireless Telecom Services</option>
          </optgroup>
          <optgroup label="Utilities">
              <option>Electric Utilities</option>
              <option>Gas Utilities</option>
              <option>Independent Power Renewable</option>
              <option>Multi-Utilities</option>
              <option>Water Utilities</option>
          </optgroup>
      </select>
     */
    private String isSecuredByPension; // Y: Yes N: No
    private String education;
    /*
      <select class="form-control">
          <option selected></option>
          <option>Professional</option>
          <option>Master</option>
          <option>Graduate</option>
          <option>Under Graduate</option>
      </select>
     */
    private String isFinanceProfessional; // Y: Yes, N: No
    private Date expectedRetirementDate;
    private String isAlive; // Y: Yes, N: No
    private Date dateLastUpdate; //Last update

    public Member() {

    }


    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SequenceNextHighValue")
    @TableGenerator(name="SequenceNextHighValue", table = "sequence_next_high_value", pkColumnName = "id", pkColumnValue = "memberid", allocationSize = 1)
    @Column(name = "memberid")
    public long getMemberid() {
        return memberid;
    }
    public void setMemberid(long memberid) {
        this.memberid = memberid;
    }

    @Column(name = "first_name", nullable = false)
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(name = "middle_name")
    public String getMiddleName() {
        return middleName;
    }
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @Column(name = "last_name")
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Column(name = "relationship")
    public String getRelationship() {
        return relationship;
    }
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    @Column(name = "birth_date")
    public Date getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    @Column(name = "gender")
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    @Column(name = "marital_status")
    public String getMaritalStatus() {
        return maritalStatus;
    }
    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    @Column(name = "email")
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "cellno")
    public String getCellNo() {
        return cellNo;
    }
    public void setCellNo(String cellNo) {
        this.cellNo = cellNo;
    }

    @Column(name = "earning_status")
    public String getEarningStatus() {
        return earningStatus;
    }

    public void setEarningStatus(String earningStatus) {
        this.earningStatus = earningStatus;
    }

    @Column(name = "profession")
    public String getProfession() {
        return profession;
    }
    public void setProfession(String profession) {
        this.profession = profession;
    }

    @Column(name = "industry")
    public String getIndustry() {
        return industry;
    }
    public void setIndustry(String industry) {
        this.industry = industry;
    }

    @Column(name = "is_secured_by_pension")
    public String getIsSecuredByPension() {
        return isSecuredByPension;
    }
    public void setIsSecuredByPension(String isSecuredByPension) {
        this.isSecuredByPension = isSecuredByPension;
    }

    @Column(name = "education")
    public String getEducation() {
        return education;
    }
    public void setEducation(String education) {
        this.education = education;
    }

    @Column(name = "is_finance_professional")
    public String getIsFinanceProfessional() {
        return isFinanceProfessional;
    }
    public void setIsFinanceProfessional(String isFinanceProfessional) {
        this.isFinanceProfessional = isFinanceProfessional;
    }

    @Column(name = "expected_retirement_date")
    public Date getExpectedRetirementDate() {
        return expectedRetirementDate;
    }
    public void setExpectedRetirementDate(Date expectedRetirementDate) {
        this.expectedRetirementDate = expectedRetirementDate;
    }

    @Column(name = "is_alive")
    public String getIsAlive() {
        return isAlive;
    }
    public void setIsAlive(String isAlive) {
        this.isAlive = isAlive;
    }

    @Column(name = "date_last_update")
    public Date getDateLastUpdate() {
        return dateLastUpdate;
    }
    public void setDateLastUpdate(Date dateLastUpdate) {
        this.dateLastUpdate = dateLastUpdate;
    }
}
