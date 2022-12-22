package com.rocket.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author yzy
 * @create 2022-12-22-22:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Message implements Serializable {
    private Integer id;
    private String date;
    private String time;
}
