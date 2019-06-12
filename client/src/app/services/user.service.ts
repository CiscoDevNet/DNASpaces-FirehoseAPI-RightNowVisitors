import { Injectable } from '@angular/core';
import { LocationUpdate } from '../models/location-update';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  
  apiURL: string = 'http://localhost:8887/api/v1';

  constructor(private http: HttpClient) {}

  public getVisitors(){
    return this.http.get<LocationUpdate[]>(this.apiURL+"/rightnowusers?mac=mac");
  }
}
