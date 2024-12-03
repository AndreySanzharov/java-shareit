package ru.practicum.shareit.user.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.SameUsersEmailExistsException;
import ru.practicum.shareit.exceptions.UserDtoException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private Map<Integer, User> users = new HashMap<>();
    private int totalId = 0;
    private final UserMapper userMapper;

    public UserDto add(UserDto userDto) {
        validateUserDto(userDto);
        User user = userMapper.fromUserDto(userDto);
        user.setId(++totalId);
        users.put(user.getId(), user);
        System.out.println(users);
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto update(int userId, UserDto userDto) {

        validateUserById(userId);
        if (userDto.getEmail() != null) {
            boolean emailExists = users.values().stream()
                    .filter(user -> user.getId() != userId)
                    .map(User::getEmail)
                    .anyMatch(userDto.getEmail()::equals);

            if (emailExists) {
                throw new SameUsersEmailExistsException("Пользователь с таким email уже существует");
            }

            users.get(userId).setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            users.get(userId).setName(userDto.getName());
        }

        return userMapper.toUserDto((users.get(userId)));
    }

    @Override
    public UserDto get(int id) {
        validateUserById(id);
        return userMapper.toUserDto(users.get(id));
    }

    @Override
    public void delete(int id) {
        validateUserById(id);
        users.remove(id);
    }

    private void validateUserDto(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getName() == null) {
            throw new UserDtoException("Почта и имя пользователя не должны быть пустыми");
        }
        boolean emailExists = users.values().stream().map(User::getEmail).anyMatch(userDto.getEmail()::equals);
        if (emailExists) {
            throw new SameUsersEmailExistsException("Пользователь с таким email уже существует");
        }
    }

    private void validateUserById(Integer userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователя с указанным id не существует");
        }
    }
}