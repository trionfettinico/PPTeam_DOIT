import { Organization } from './../../../../model/organization';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MenuController, AlertController, NavController } from '@ionic/angular';
import { DataService } from 'src/app/services/data.service';
import { GlobalsService } from 'src/app/services/globals.service';

@Component({
  selector: 'app-view-organization',
  templateUrl: './view-organization.page.html',
  styleUrls: ['./view-organization.page.scss'],
})
export class ViewOrganizationPage {
  organization:Organization;

  constructor(
    private route:ActivatedRoute, 
    private nav:NavController,
    public data:DataService,
    private menuCtrl:MenuController, 
    private http:HttpClient,
    private globals:GlobalsService,
    private alertCtrl:AlertController,
    ) { 
    const id = this.route.snapshot.params["id"];
    this.organization = this.data.getOrganizationt(id);
    this.menuCtrl.enable(false);
  }

  onBack(){
    this.menuCtrl.enable(true);
    this.nav.navigateBack(["/list-of-organizations"], { queryParams: { 'refresh': 1 } });
  }

  deleteOrganization(){
    this.http.delete(this.globals.organizationApiUrl+this.organization.name).subscribe(
      async res => {
        if(this.organization.creatorMail==this.data.userMail){
          this.data.quitFromOrg();
        }
        console.log("organization removed successfully");
        this.data.removeOrganizationt(this.organization);
        const alert = await this.alertCtrl.create({
          cssClass: 'my-custom-class',
          header: 'Cancellata',
          message: 'Organizzazione Cancellata.',
          buttons: ['OK']
        });
      
        await alert.present();
      },
      err => {
        console.log("error on delete organization: "+err);
      },
    );
    this.onBack();
  }

  async addExpert(){
    const alert = await this.alertCtrl.create({
      cssClass: 'my-custom-class',
      header: 'Scegli',
      message: 'Che tipo di esperto ?',
      buttons: [
        {
          text: 'Interno',
          handler: () => {
            this.nav.navigateForward(["/add-collaborator"]);
          }
        }, {
          text: 'Esterno',
          handler: () => {
            console.log('Confirm Okay');
          }
        }
      ]
    });
  
    await alert.present();
   }

}


