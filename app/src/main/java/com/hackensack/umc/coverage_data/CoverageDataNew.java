package com.hackensack.umc.coverage_data;

import com.hackensack.umc.patient_data.Identifier;
import com.hackensack.umc.patient_data.Issuer;
import com.hackensack.umc.patient_data.Period;
import com.hackensack.umc.patient_data.Subscriber;
import com.hackensack.umc.patient_data.Text;

import java.util.ArrayList;

/**
 * Created by prerana_katyarmal on 10/26/2015.
 */
public class CoverageDataNew
{


    private String group;
    private String resourceType;
    private SubscriberId subscriberId;
    private ArrayList<Extension> extension;

    public CoverageDataNew(String resourceType) {
        this.resourceType=resourceType;
    }


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public SubscriberId getSubscriberId ()
    {
        return subscriberId;
    }

    public ArrayList<Extension> getExtension() {
        return extension;
    }

    public void setExtension(ArrayList<Extension> extension) {
        this.extension = extension;
    }

    public void setSubscriberId (SubscriberId subscriberId)
    {

        this.subscriberId = subscriberId;
    }


/*{
	"resourceType": "Coverage",
	"group": "<<group number>>",
	"subscriberId": {
		"use": "office",
		"value": "<<SubscriberID>>"
	},
	"extension": [{
		"url": "SearchTag",
		"value": [{
			"InsuranceName": "<<insurance name>>",
			"SubscriberName": "<<Name>>",
			"SubscriberDateOfBirth": "<<yyyy-mm-dd>>",
			"MemberNumber": "<<membernumber>>",
			"PatientId": "<<MRN>>"

		}]
	}]
}*/


}

