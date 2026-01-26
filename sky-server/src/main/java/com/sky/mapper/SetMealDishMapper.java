package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    void insert(List<SetmealDish> setMealDishes);

    @Delete("delete from setmeal_dish where setmeal_id = #{setMealId}")
    void deleteBySetMealId(Long setMealId);

    @Select("select * from setmeal_dish where setmeal_id = #{setMealId}")
    List<SetmealDish> getById(Long setMealId);
}
