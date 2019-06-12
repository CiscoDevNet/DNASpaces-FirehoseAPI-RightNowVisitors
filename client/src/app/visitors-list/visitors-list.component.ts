import { Component, OnInit } from '@angular/core';
import { UserService } from '../services/user.service';
import { LocationUpdate } from '../models/location-update';

@Component({
  selector: 'app-visitors-list',
  templateUrl: './visitors-list.component.html',
  styleUrls: ['./visitors-list.component.css']
})
export class VisitorsListComponent implements OnInit {

  public visitors : LocationUpdate[] = [];
  public filteredvisitors : LocationUpdate[] = [];
  public searchText : string;
  private timeIntevalSeconds : number = 5;

  constructor(private userService:UserService) { }

  ngOnInit() {
    setInterval(()=> { this.load() }, this.timeIntevalSeconds * 1000);
    this.reload();
  }

  search(){
    this.filteredvisitors = this.visitors.filter(visitor => (visitor.mac_address.includes(this.searchText) || visitor.location_name.includes(this.searchText)));
  }

  reload(){
    this.searchText = "";
    this.load();
  }

  load(){
    this.userService.getVisitors().subscribe((data: LocationUpdate[]) => {
      this.visitors = data;
      if(!this.searchText){
        this.filteredvisitors = data;
      }
    } );
  }

  
}
